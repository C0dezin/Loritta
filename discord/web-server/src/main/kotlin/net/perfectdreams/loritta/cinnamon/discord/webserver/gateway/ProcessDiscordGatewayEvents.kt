package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import org.postgresql.jdbc.PgConnection
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.*

/**
 * Processes Discord Gateway Events stored on a PostgreSQL table
 *
 * Yeah, that's a Devious SQL™ moment https://www.crunchydata.com/blog/message-queuing-using-native-postgresql
 */
class ProcessDiscordGatewayEvents(
    private val totalEventsPerBatch: Int,
    private val commitOnEveryXStatements: Int,
    val totalConnections: Int,
    val connectionId: Int,
    replicaInstance: ReplicaInstanceConfig,
    private val queueDatabaseDataSource: HikariDataSource,
    // Shard ID -> ProxiedKordGateway
    private val proxiedKordGateways: Map<Int, ProxiedKordGateway>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val DISCORD_GATEWAY_EVENTS_TABLE = "discordgatewayevents"
        private const val NOTIFICATION_TIMEOUT_MILLIS = 15_000
    }

    var totalEventsProcessed = 0L
    var totalPollLoopsCount = 0L
    var lastPollDuration: Duration? = null
    var lastBlockDuration: Duration? = null
    var isBlockedForNotifications = false

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val shardsHandledByThisProcessor = (replicaInstance.minShard..replicaInstance.maxShard step totalConnections).map {
        it + connectionId
    }
    private val lastTimeShardsChecked = shardsHandledByThisProcessor.associateWith { 0L }.toMutableMap()

    // Shard ID -> SQL Statement
    private val sqlStatements = shardsHandledByThisProcessor.associate {
        // While using "shard BETWEEN ${replicaInstance.minShard} AND ${replicaInstance.maxShard} AND" and "shard % 8 = 0" seems obvious, using those seems to cause PostgreSQL to query
        // EVERY SINGLE TABLE!!
        // Limit  (cost=2.71..9.64 rows=1 width=103) (actual time=0.112..0.113 rows=0 loops=1)
        //   ->  LockRows  (cost=2.71..425.57 rows=61 width=103) (actual time=0.112..0.113 rows=0 loops=1)
        //         ->  Merge Append  (cost=2.71..424.96 rows=61 width=103) (actual time=0.111..0.113 rows=0 loops=1)
        //               Sort Key: discordgatewayevents.id
        //               ->  Index Scan using discordgatewayevents_shard_0_pkey on discordgatewayevents_shard_0 discordgatewayevents_1  (cost=0.14..23.36 rows=1 width=1115) (actual time=0.028..0.028 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_1_pkey on discordgatewayevents_shard_1 discordgatewayevents_2  (cost=0.15..26.65 rows=4 width=86) (actual time=0.010..0.010 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_2_pkey on discordgatewayevents_shard_2 discordgatewayevents_3  (cost=0.15..26.65 rows=4 width=86) (actual time=0.005..0.005 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_3_pkey on discordgatewayevents_shard_3 discordgatewayevents_4  (cost=0.15..26.65 rows=4 width=86) (actual time=0.009..0.009 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_4_pkey on discordgatewayevents_shard_4 discordgatewayevents_5  (cost=0.15..26.65 rows=4 width=86) (actual time=0.004..0.004 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_5_pkey on discordgatewayevents_shard_5 discordgatewayevents_6  (cost=0.15..26.65 rows=4 width=86) (actual time=0.004..0.004 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        //               ->  Index Scan using discordgatewayevents_shard_6_pkey on discordgatewayevents_shard_6 discordgatewayevents_7  (cost=0.15..26.65 rows=4 width=86) (actual time=0.005..0.005 rows=0 loops=1)
        //                     Filter: ((shard % 8) = 0)
        // ...
        // And trying to manually add shard = 1 OR shard = 2... does fix the performance issue a bit, but it is still bad
        // loritta_queues=# EXPLAIN ANALYZE SELECT "id", "type", "shard", "payload" FROM discordgatewayevents WHERE shard = 695 OR shard = 703 ORDER BY id LIMIT 10000;
        //                                                                                 QUERY PLAN
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Limit  (cost=13.04..13.05 rows=3 width=903) (actual time=0.544..0.546 rows=0 loops=1)
        //   ->  Sort  (cost=13.04..13.05 rows=3 width=903) (actual time=0.544..0.545 rows=0 loops=1)
        //         Sort Key: discordgatewayevents.id
        //         Sort Method: quicksort  Memory: 25kB
        //         ->  Append  (cost=2.47..13.01 rows=3 width=903) (actual time=0.540..0.541 rows=0 loops=1)
        //               ->  Bitmap Heap Scan on discordgatewayevents_shard_695 discordgatewayevents_1  (cost=2.47..3.60 rows=1 width=1097) (actual time=0.333..0.334 rows=0 loops=1)
        //                     Recheck Cond: ((shard = 695) OR (shard = 703))
        //                     ->  BitmapOr  (cost=2.47..2.47 rows=1 width=0) (actual time=0.030..0.031 rows=0 loops=1)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_695_shard_idx  (cost=0.00..1.23 rows=1 width=0) (actual time=0.029..0.029 rows=1044 loops=1)
        //                                 Index Cond: (shard = 695)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_695_shard_idx  (cost=0.00..1.23 rows=1 width=0) (actual time=0.001..0.001 rows=0 loops=1)
        //                                 Index Cond: (shard = 703)
        //               ->  Bitmap Heap Scan on discordgatewayevents_shard_703 discordgatewayevents_2  (cost=7.13..9.37 rows=2 width=807) (actual time=0.206..0.207 rows=0 loops=1)
        //                     Recheck Cond: ((shard = 695) OR (shard = 703))
        //                     ->  BitmapOr  (cost=7.13..7.13 rows=2 width=0) (actual time=0.029..0.030 rows=0 loops=1)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_703_shard_idx  (cost=0.00..2.46 rows=1 width=0) (actual time=0.006..0.006 rows=0 loops=1)
        //                                 Index Cond: (shard = 695)
        //                           ->  Bitmap Index Scan on discordgatewayevents_shard_703_shard_idx  (cost=0.00..4.67 rows=2 width=0) (actual time=0.023..0.023 rows=655 loops=1)
        //                                 Index Cond: (shard = 703)
        // ...
        // What can we do?
        // We will abuse the fact that we create a different table for each shard, and then query it directly using different statements!
        // loritta_queues=# EXPLAIN ANALYZE SELECT "id", "type", "shard", "payload" FROM discordgatewayevents_shard_384 ORDER BY id LIMIT 10000;
        //                                                                                  QUERY PLAN
        //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        // Limit  (cost=0.13..31.06 rows=8 width=1298) (actual time=0.047..0.047 rows=0 loops=1)
        //   ->  Index Scan using discordgatewayevents_shard_384_pkey on discordgatewayevents_shard_384  (cost=0.13..31.06 rows=8 width=1298) (actual time=0.046..0.046 rows=0 loops=1)
        // Planning Time: 0.053 ms
        // Execution Time: 0.056 ms
        // (4 rows)
        // Look, ok, 0.056ms for each shard, let's do some calculations then
        // The full:tm: 40 shards query with WHERE shard = 1 OR ... = 43.639 ms
        // Direct Table access = 0.056
        // 0.056 * 40 = 2.24 ms!!!
        // So let's go... even further beyond!
        val trueTableName = DISCORD_GATEWAY_EVENTS_TABLE + "_shard_$it"
        it to """DELETE FROM $trueTableName USING (SELECT "id", "shard", "payload" FROM $trueTableName ORDER BY id FOR UPDATE SKIP LOCKED LIMIT $totalEventsPerBatch) q WHERE q.id = $trueTableName.id RETURNING $trueTableName.*;"""
    }

    @OptIn(ExperimentalTime::class)
    suspend fun run() {
        for ((shardId, statement) in sqlStatements) {
            logger.info { "SQL Statement to be used in the event processor (shard ID: $shardId, conn ID: $connectionId): $statement" }
        }

        while (true) {
            try {
                // Instead of using the PostgreSQLNotificationListener class, we will listen it here
                // Why? Because we want the notification listener connection to BLOCK while we are processing events
                // This way, we avoid a hot loop where a lot of CPU usage is being used by getting all received notifications
                queueDatabaseDataSource.connection
                    .unwrap(PgConnection::class.java)
                    .use { connection ->
                        for (shardId in shardsHandledByThisProcessor) {
                            val stmt = connection.createStatement()
                            stmt.execute("LISTEN gateway_events_shard_$shardId;")
                            stmt.close()
                        }
                        connection.commit()

                        while (true) {
                            // We will use a 60s timeout, to avoid blocking forever
                            // If the notification list is empty, we will add all shards of this processor to the shardsWithNewEvents set
                            isBlockedForNotifications = true
                            val (notifications, time) = measureTimedValue {
                                connection.getNotifications(NOTIFICATION_TIMEOUT_MILLIS)
                            }
                            isBlockedForNotifications = false
                            this.lastBlockDuration = time

                            val shardsWithNewEvents = mutableSetOf<Int>()

                            for (notification in notifications) {
                                // While we *could* parse the "parameter", let's just avoid parsing it to avoid unnecessary memory allocations
                                // Besides, we already know that the only notification that will come from the channel is "babe wake up new gateway event on shard 5 just dropped"
                                val shardId = notification.name.substringAfterLast("_").toInt()
                                shardsWithNewEvents.add(shardId)
                            }

                            val manuallyQueuedShards = mutableSetOf<Int>()

                            for ((shardId, lastCheck) in lastTimeShardsChecked) {
                                if (System.currentTimeMillis() - lastCheck >= NOTIFICATION_TIMEOUT_MILLIS) {
                                    manuallyQueuedShards.add(shardId)
                                    shardsWithNewEvents.add(shardId)
                                }
                            }

                            if (manuallyQueuedShards.isNotEmpty())
                                logger.warn { "Manually queueing shards $manuallyQueuedShards beause their last successful gateway notification check on $connectionId was more than ${NOTIFICATION_TIMEOUT_MILLIS}ms ago, so maybe new gateway events notifications aren't being triggered, so we will handle it manually..." }

                            // Let's get out of here if there isn't any shards for us
                            // This can happen if the notification returns an empty list, and we didn't have any shards triggered via the lastTimeShardsChecked
                            if (shardsWithNewEvents.isEmpty())
                                continue

                            val now = System.currentTimeMillis()
                            for (shard in shardsWithNewEvents) {
                                lastTimeShardsChecked[shard] = now
                            }

                            // The shardsWithNewEvents set should have what shards have new events, yay!
                            val sqlStatementsForTheShards = sqlStatements
                                .filterKeys { it in shardsWithNewEvents }

                            var processedStatements = 0
                            var statementsRequeued = 0

                            val duration = measureTime {
                                val statements = LinkedBlockingQueue<String>()
                                statements.addAll(sqlStatementsForTheShards.values)

                                while (statements.isNotEmpty()) {
                                    val sql = statements.remove()

                                    var processedOnThisStatement = 0
                                    val selectStatement = connection.prepareStatement(sql)
                                    val rs = selectStatement.executeQuery()

                                    var count = 0

                                    while (rs.next()) {
                                        val shardId = rs.getInt("shard")
                                        val gatewayPayload = rs.getString("payload")

                                        val discordEvent = KordDiscordEventUtils.parseEventFromString(gatewayPayload)

                                        if (discordEvent != null) {
                                            // Emit the event to our proxied instances
                                            val proxiedKordGateway = proxiedKordGateways[shardId] ?: error("Received event for shard ID $shardId, but we don't have a ProxiedKordGateway instance associated with it!")
                                            coroutineScope.launch {
                                                proxiedKordGateway.events.emit(discordEvent)
                                            }
                                        } else {
                                            logger.warn { "Unknown Discord event received! We are going to ignore the event... kthxbye!" }
                                        }

                                        count++
                                        totalEventsProcessed++
                                        processedOnThisStatement++
                                    }

                                    if (processedOnThisStatement == totalEventsPerBatch) {
                                        statementsRequeued++
                                        statements.add(sql)
                                    }

                                    processedStatements++

                                    // Auto commit on every X statements
                                    // This is useful because committing marks the tuple as dead on PostgreSQL side, and depending on how many events we are processing, it is useful to commit before we finish everything
                                    if (statements.isNotEmpty() && processedStatements % commitOnEveryXStatements == 0) {
                                        logger.info { "Processed $processedStatements statements, triggering a commit request..." }
                                        connection.commit()
                                    }
                                }

                                connection.commit()
                            }

                            lastPollDuration = duration
                            totalPollLoopsCount++

                            // To avoid flooding the console with "owo let's requeue the statement!" we will only send how many statements were requeued
                            if (statementsRequeued != 0)
                                logger.warn { "On this query, we requeued $statementsRequeued statements because the query count was the same value as the total events per batch! This may mean that there is more pending events than what the batch is retrieving, and that's why it was requeued... Consider increasing the totalEventsPerBatch setting!" }
                        }
                    }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while polling pending Discord gateway events!" }
            }
        }
    }
}