package net.perfectdreams.loritta.deviousfun.gateway

import dev.kord.gateway.Close
import dev.kord.gateway.Event
import dev.kord.gateway.InvalidSession
import dev.kord.gateway.Ready
import dev.kord.gateway.ratelimit.IdentifyRateLimiter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.upsert
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ConcurrentLoginBuckets
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

/**
 * Implements rate limiting based on Discord's "sharding for very large bots", using Redis.
 *
 * https://redis.io/docs/reference/patterns/distributed-locks/
 * https://discord.com/developers/docs/topics/gateway#sharding-for-large-bots
 */
class ParallelIdentifyRateLimiter(
    private val shardId: Int,
    private val bucketId: Int,
    override val maxConcurrency: Int,
    private val pudding: Pudding,
    private val gatewayStatus: MutableStateFlow<DeviousGateway.Status>,
    private val identifyLock: Mutex
) : IdentifyRateLimiter {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val random = Random.Default

    override suspend fun consume(shardId: Int, events: SharedFlow<Event>) {
        // This is used to lock idenitfies until Loritta is fully booted
        identifyLock.withLock {}

        // PostgreSQL should handle conflicts by itself, so if two instances try to edit the same column at the same time, a concurrent modification exception will happen
        // If randomKey == null, then this bucket is already being used
        // If randomKey != null, then this bucket isn't being used
        try {
            val randomKey = pudding.transaction {
                val currentStatus = ConcurrentLoginBuckets.select {
                    ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.lockedAt greaterEq Instant.now()
                        .minusSeconds(60))
                }.firstOrNull()

                if (currentStatus != null) {
                    return@transaction null
                } else {
                    val newRandomKey = Base64.getEncoder().encodeToString(random.nextBytes(20))

                    ConcurrentLoginBuckets.upsert(ConcurrentLoginBuckets.id) {
                        it[ConcurrentLoginBuckets.id] = bucketId
                        it[ConcurrentLoginBuckets.randomKey] = newRandomKey
                        it[ConcurrentLoginBuckets.lockedAt] = Instant.now()
                    }

                    return@transaction newRandomKey
                }
            }

            if (randomKey != null) {
                // We need to create a new coroutine to let the gateway login
                GlobalScope.launch {
                    gatewayStatus.value = DeviousGateway.Status.IDENTIFYING

                    // Acquired lock! We can login, yay!! :3
                    logger.info { "Successfully acquired lock for bucket $bucketId (shard $shardId)!" }

                    val result = withTimeoutOrNull(15.seconds) {
                        events.first {
                            it is Ready || it is InvalidSession || it is Close
                        }
                    }

                    if (result is Ready) {
                        logger.info { "Bucket $bucketId (shard $shardId) successfully logged in!" }

                        // After it is ready, we will wait 5000ms to release the lock
                        delay(5.seconds)
                    }

                    logger.info { "Trying to release lock for bucket $bucketId (shard $shardId)... - Successfully logged in? ${result is Ready}" }

                    val deletedBucketsCount = pudding.transaction {
                        ConcurrentLoginBuckets.deleteWhere { ConcurrentLoginBuckets.id eq bucketId and (ConcurrentLoginBuckets.randomKey eq randomKey) }
                    }

                    when (deletedBucketsCount) {
                        0 -> logger.warn { "Couldn't release lock for bucket $bucketId (shard $shardId) because our random key does not match or the bucket was already released!" }
                        else -> logger.info { "Successfully released lock for bucket $bucketId (shard $shardId)!" }
                    }
                }
            } else {
                // Couldn't acquire lock, let's wait...
                logger.info { "Couldn't acquire lock for bucket $bucketId (shard $shardId), let's wait and try again later..." }
                gatewayStatus.value = DeviousGateway.Status.WAITING_FOR_BUCKET
                delay(1_000)
                // And try again!
                return consume(shardId, events)
            }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while trying to acquire lock for bucket $bucketId (shard $shardId), let's wait and try again later..." }

            delay(15_000)
            return consume(shardId, events)
        }
    }
}