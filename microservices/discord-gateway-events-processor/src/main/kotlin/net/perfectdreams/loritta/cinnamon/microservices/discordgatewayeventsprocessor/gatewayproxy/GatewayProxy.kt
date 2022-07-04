package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.GatewayEvent
import java.io.Closeable
import kotlin.math.pow

/**
 * Receives Discord gateway events via an WebSocket connection
 */
class GatewayProxy(
    val url: String,
    val authorizationToken: String,
    val onMessageReceived: (GatewayEvent) -> (Unit)
) : Closeable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val http = HttpClient {
        install(WebSockets) {
            this.pingInterval = 15_000
        }

        install(HttpTimeout)
    }

    var session: ClientWebSocketSession? = null
    var connectionTries = 1

    fun start() {
        coroutineScope.launch {
            while (true) {
                logger.info { "Connecting to Gateway..." }
                connect()
                connectionTries++
                val delay = (1.1.pow(connectionTries.toDouble()) * 50).toLong()
                    .coerceAtMost(60_000)
                logger.info { "Connection closed! Reconnecting in ${delay}ms..." }
                delay(delay)
            }
        }
    }

    private suspend fun connect() {
        try {
            val newSession = http.webSocketSession(
                "ws://${url}"
            ) {
                // workaround until https://youtrack.jetbrains.com/issue/KTOR-4419 is fixed
                // otherwise the gateway connection will die and fail to reconnect
                timeout {
                    requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
                }

                header("Authorization", authorizationToken)
            }

            session = newSession

            for (event in newSession.incoming) {
                when (event) {
                    is Frame.Text -> {
                        connectionTries = 0 // On a successful connection, reset the try counter
                        onMessageReceived.invoke(GatewayEvent(event.data.toString(Charsets.UTF_8)))
                    }
                    is Frame.Binary -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Close -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Ping -> {} // No need to handle this / It doesn't seem to be sent to us
                    is Frame.Pong -> {} // No need to handle this / It doesn't seem to be sent to us
                }
            }
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while listening to the session!" }
        }
    }

    override fun close() {
        runBlocking {
            coroutineScope.cancel()
            session?.close()
            http.close()
        }
    }
}