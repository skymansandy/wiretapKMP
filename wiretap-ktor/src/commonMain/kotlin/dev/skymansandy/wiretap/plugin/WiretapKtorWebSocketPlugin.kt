package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.util.AttributeKey
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.Volatile

private val WiretapSocketIdKey = AttributeKey<Long>("WiretapSocketId")

/**
 * Ktor client plugin that intercepts WebSocket sessions to log
 * connections and messages via Wiretap.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(WebSockets)
 *     install(WiretapKtorHttpPlugin)
 *     install(WiretapKtorWebSocketPlugin)
 * }
 * ```
 *
 * Note: This plugin hooks into 101 Switching Protocols responses.
 * For full outgoing message interception, use [WiretapWebSocketSession]
 * to wrap your session.
 */
val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {

    val deps = WsPluginDeps()

    onResponse { response ->
        // Only intercept WebSocket upgrades (status 101)
        if (response.status.value != 101) return@onResponse

        val url = response.call.request.url.toString().toWebSocketUrl()
        val requestHeaders = response.call.request.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }

        val socketId = deps.socketLogManager.createSocket(
            SocketConnection(
                url = url,
                requestHeaders = requestHeaders,
                status = SocketStatus.Open,
                timestamp = currentTimeMillis(),
                protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" },
            ),
        )

        // Store socket ID for later use
        response.call.request.attributes.put(WiretapSocketIdKey, socketId)
    }
}

/**
 * Extension to wrap a Ktor [DefaultClientWebSocketSession] for Wiretap logging.
 *
 * Requires [WiretapKtorWebSocketPlugin] to be installed in the HttpClient.
 * Returns `null` if the plugin is not installed.
 *
 * ```kotlin
 * client.webSocket("wss://example.com/ws") {
 *     val session = this.wiretapWrap() ?: return@webSocket
 *     session.send(Frame.Text("hello"))
 *     for (frame in session.incoming) { ... }
 * }
 * ```
 */
fun DefaultClientWebSocketSession.wiretapWrap(): WiretapWebSocketSession? {
    val socketId = call.request.attributes.getOrNull(WiretapSocketIdKey)
        ?: return null
    if (socketId < 0) return null

    val deps = WsPluginDeps()
    return WiretapWebSocketSession(this, socketId, deps.socketLogManager)
}

/**
 * Wraps a [DefaultClientWebSocketSession] to log all sent and received messages.
 *
 * Automatically detects session completion (timeout, server close, error) and
 * updates the socket status accordingly — no manual `markClosed()`/`markFailed()` needed.
 */
class WiretapWebSocketSession internal constructor(
    private val delegate: DefaultClientWebSocketSession,
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
) {

    /**
     * Channel of incoming frames with automatic logging.
     * All frame types (Text, Binary, Ping, Pong, Close) are logged as they are consumed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val incoming: ReceiveChannel<Frame> = delegate.produce {
        for (frame in delegate.incoming) {
            logFrame(frame, SocketMessageType.Received)
            send(frame)
        }
    }

    @Volatile
    private var statusUpdated = false

    init {
        installAutoClose()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun installAutoClose() {
        delegate.coroutineContext[Job]?.invokeOnCompletion { cause ->
            if (statusUpdated) return@invokeOnCompletion

            statusUpdated = true
            val url = delegate.call.request.url.toString().toWebSocketUrl()
            runBlocking {
                if (cause != null && cause !is CancellationException) {
                    socketLogManager.updateSocket(
                        SocketConnection(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Failed,
                            failureMessage = cause.message ?: cause::class.simpleName ?: "Unknown error",
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                } else {
                    val closeReason = runCatching { delegate.closeReason.getCompleted() }.getOrNull()
                    socketLogManager.updateSocket(
                        SocketConnection(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Closed,
                            closeCode = closeReason?.code?.toInt(),
                            closeReason = closeReason?.message ?: if (cause is CancellationException) "Cancelled" else null,
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                }
            }
        }
    }

    suspend fun send(frame: Frame) {
        logFrame(frame, SocketMessageType.Sent)
        delegate.send(frame)
    }

    suspend fun close(code: Short = 1000.toShort(), reason: String? = null) {
        statusUpdated = true

        val url = delegate.call.request.url.toString().toWebSocketUrl()
        socketLogManager.updateSocket(
            SocketConnection(
                id = socketId,
                url = url,
                status = SocketStatus.Closed,
                closeCode = code.toInt(),
                closeReason = reason,
                closedAt = currentTimeMillis(),
                timestamp = currentTimeMillis(),
            ),
        )

        delegate.close()
    }

    private suspend fun logFrame(frame: Frame, direction: SocketMessageType) {
        val (contentType, content, byteCount) = when (frame) {
            is Frame.Text -> {
                val text = frame.readText()
                Triple(SocketContentType.Text, text, text.encodeToByteArray().size.toLong())
            }

            is Frame.Binary -> {
                val bytes = frame.readBytes()
                Triple(SocketContentType.Binary, "[Binary: ${bytes.size} bytes]", bytes.size.toLong())
            }

            is Frame.Ping -> Triple(SocketContentType.Ping, "", frame.data.size.toLong())
            is Frame.Pong -> Triple(SocketContentType.Pong, "", frame.data.size.toLong())
            is Frame.Close -> {
                val bytes = frame.data
                val closeContent = if (bytes.size >= 2) {
                    val closeCode = (bytes[0].toInt() and 0xFF shl 8) or (bytes[1].toInt() and 0xFF)
                    val closeReason = if (bytes.size > 2) bytes.decodeToString(2, bytes.size) else ""
                    if (closeReason.isNotEmpty()) "$closeCode $closeReason" else "$closeCode"
                } else {
                    ""
                }
                Triple(SocketContentType.Close, closeContent, bytes.size.toLong())
            }

            else -> return
        }

        socketLogManager.logSocketMsg(
            SocketMessage(
                socketId = socketId,
                direction = direction,
                contentType = contentType,
                content = content,
                byteCount = byteCount,
                timestamp = currentTimeMillis(),
            ),
        )
    }
}

private fun String.toWebSocketUrl(): String =
    replaceFirst("http://", "ws://")
        .replaceFirst("https://", "wss://")

private class WsPluginDeps : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    val socketLogManager: SocketLogManager by inject()
}
