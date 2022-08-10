package net.perfectdreams.loritta.cinnamon.discord.webserver

import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.EventAnalyticsTask
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProcessDiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.ProxyDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.webserver.InteractionsServer
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.NewDiscordGatewayEventNotification
import net.perfectdreams.loritta.cinnamon.pudding.utils.LorittaNotificationListener
import net.perfectdreams.loritta.cinnamon.pudding.utils.PostgreSQLNotificationListener
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys
import kotlin.time.Duration.Companion.seconds

class LorittaCinnamonWebServer(
    val config: RootConfig,
    private val languageManager: LanguageManager,
    private val services: Pudding,
    private val queueConnection: HikariDataSource,
    private val http: HttpClient,
    private val replicaId: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val replicaInstance = config.replicas.instances.firstOrNull { it.replicaId == replicaId } ?: error("Missing replica configuration for replica ID $replicaId")

    private val proxyDiscordGatewayManager = ProxyDiscordGatewayManager(
        config.discordShards.totalShards,
        replicaInstance,
        services
    )

    private val discordGatewayEventsProcessors = (0 until config.queueDatabase.connections).map {
        ProcessDiscordGatewayEvents(
            config.totalEventsPerBatch,
            config.queueDatabase.commitOnEveryXStatements,
            config.queueDatabase.connections,
            it,
            replicaInstance,
            queueConnection,
            proxyDiscordGatewayManager.gateways
        )
    }

    // The same thing as a above, but in a ShardId -> Processor Map, to improve performance (yay)
    private val shardToDiscordGatewayEventsProcessors = discordGatewayEventsProcessors.flatMap {
        it.shardsHandledByThisProcessor.map { shardId -> shardId to it }
    }.toMap()

    private val stats = mutableMapOf<Int, Pair<Long, Long>>()

    private val gatewayQueueNotificationCoroutineScope = CoroutineScope(Dispatchers.IO)

    val gatewayQueueNotificationListener = PostgreSQLNotificationListener(
        queueConnection,
        mapOf(
            "gateway_events" to {
                val lorittaNotification = JsonIgnoreUnknownKeys.decodeFromString<LorittaNotification>(it)

                if (lorittaNotification is NewDiscordGatewayEventNotification) {
                    gatewayQueueNotificationCoroutineScope.launch {
                        val processor = shardToDiscordGatewayEventsProcessors[lorittaNotification.shardId] ?: return@launch // Not for us, bye

                        processor.shardsMutex.withLock {
                            processor.shardsWithNewEvents.add(lorittaNotification.shardId)
                        }

                        // We will use trySend because we don't care if the notification is lost
                        processor.notificationChannelTrigger.trySend(Unit)
                    }
                }
            }
        )
    ).also {
        Thread(null, it, "Gateway Events PostgreSQL Notification Listener").start()
    }

    fun start() {
        logger.info { "Creating gateway events queue tables..." }
        // To avoid initializing Exposed for our "queueConnection" just to create a table, we will create the table manually with SQL statements (woo, scary!)
        // It is more cumbersome, but hey, it works!
        queueConnection.connection.use {
            // Trying to create indexes/tables is expensive because it seems PostgreSQL tries to lock the table, even if we are using "IF NOT EXISTS"
            // To workaround this, we will check if all tables are created and, if they are, we will ignore the update
            val tables = mutableListOf<String>()

            it.prepareStatement("SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema';")
                .executeQuery()
                .let {
                    while (it.next()) {
                        tables.add(it.getString("tablename"))
                    }
                }

            val areAllPresent = ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE in tables && (0 until config.discordShards.totalShards)
                .all {
                    "${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE}_shard_$it" in tables
                }

            if (!areAllPresent) {
                logger.info { "Creating gateway events queue tables... hang tight!" }

                val statement = it.createStatement()
                // We will create a UNLOGGED table because it is faster, but if PostgreSQL crashes, all data will be lost
                // Because it is a table that holds gateway events, we don't really care if we can lose all data, as long as it is fast!
                val sql = buildString {
                    append(
                        """
                CREATE UNLOGGED TABLE IF NOT EXISTS ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE} (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY,
                    type TEXT NOT NULL,
                    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
                    shard INTEGER NOT NULL,
                    payload JSONB NOT NULL,
                    PRIMARY KEY (id, shard)
                ) PARTITION BY RANGE (shard);
                
                CREATE INDEX IF NOT EXISTS ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE}_type ON ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE} (type);
                CREATE INDEX IF NOT EXISTS ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE}_shard ON ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE} (shard);
            """.trimIndent()
                    )

                    // Now create a partition for each shard
                    repeat(config.discordShards.totalShards) { shardId ->
                        append(
                            """
                    CREATE UNLOGGED TABLE IF NOT EXISTS ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE}_shard_$shardId PARTITION OF ${ProcessDiscordGatewayEvents.DISCORD_GATEWAY_EVENTS_TABLE}
                        FOR VALUES FROM ($shardId) TO (${shardId + 1});
                """.trimIndent()
                        )
                    }
                }

                statement.executeUpdate(sql)
            } else {
                logger.info { "All gateway events queue tables are present, so we won't try to create them..." }
            }

            it.commit()
        }

        logger.info { "Successfully created gateway events queue tables!" }

        val cinnamon = LorittaCinnamon(
            proxyDiscordGatewayManager,
            config.cinnamon,
            languageManager,
            services,
            http
        )

        cinnamon.start()

        // Start processing gateway events
        for (processor in discordGatewayEventsProcessors) {
            GlobalScope.launch(Dispatchers.IO) {
                processor.run()
            }
        }

        GlobalScope.launch {
            while (true) {
                // Sometimes processors can get stuck waiting for notifications, which can happen because Loritta can stop sending new gateway events
                // if the queue is too large.
                // But because Loritta stops sending new events, it also stops sending new events notifications, so it gets stuck waiting for new events
                // To work around this, every second we will trigger a poll request for all shards
                logger.info { "Triggering a manual gateway event poll..." }
                discordGatewayEventsProcessors.forEach {
                    it.shardsMutex.withLock {
                        it.shardsWithNewEvents.addAll(it.shardsHandledByThisProcessor)
                    }
                    it.notificationChannelTrigger.trySend(Unit)
                }

                delay(1.seconds)
            }
        }

        cinnamon.addAnalyticHandler { logger ->
            val statsValues = stats.values
            val previousEventsProcessed = statsValues.sumOf { it.first }
            val previousPollLoopsCheck = statsValues.sumOf { it.second }

            val totalEventsProcessed = discordGatewayEventsProcessors.sumOf { it.totalEventsProcessed }
            val totalPollLoopsCheck = discordGatewayEventsProcessors.sumOf { it.totalPollLoopsCount }

            logger.info { "Total Discord Events processed: $totalEventsProcessed; (+${totalEventsProcessed - previousEventsProcessed})" }
            logger.info { "Total Poll Loops: $totalPollLoopsCheck; (+${totalPollLoopsCheck - previousPollLoopsCheck})" }
            for (processor in discordGatewayEventsProcessors) {
                val previousStats = stats[processor.connectionId] ?: Pair(0L, 0L)
                logger.info { "Processor shardId % ${processor.totalConnections} == ${processor.connectionId}: Discord Events processed: ${processor.totalEventsProcessed} (+${processor.totalEventsProcessed - previousStats.first}); Current Poll Loops Count: ${processor.totalPollLoopsCount} (+${processor.totalPollLoopsCount - previousStats.second}); Last poll took ${processor.lastPollDuration} to complete" }
                stats[processor.connectionId] = Pair(processor.totalEventsProcessed, processor.totalPollLoopsCount)
            }
        }

        val interactionsServer = InteractionsServer(
            cinnamon.interaKTions,
            config.httpInteractions.publicKey
        )

        interactionsServer.start()
    }
}