package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.Volatile

/**
 * Ktor client plugin that intercepts WebSocket sessions to log
 * connections and messages via Wiretap.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(WebSockets)
 *     install(WiretapKtorPlugin)
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

        val socketId = deps.orchestrator.openSocket(
            SocketEntry(
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

internal val WiretapSocketIdKey = io.ktor.util.AttributeKey<Long>("WiretapSocketId")

/**
 * Extension to wrap a Ktor [DefaultClientWebSocketSession] for Wiretap logging.
 *
 * This intercepts both incoming and outgoing frames. Use it like:
 * ```kotlin
 * client.webSocket("wss://example.com/ws") {
 *     val session = this.wiretapWrap()
 *     session.send(Frame.Text("hello"))
 *     for (frame in session.incoming) { ... }
 * }
 * ```
 */
suspend fun DefaultClientWebSocketSession.wiretapWrap(): WiretapWebSocketSession {
    val deps = WsPluginDeps()
    val socketId = call.request.attributes.getOrNull(WiretapSocketIdKey)
    val actualSocketId = if (socketId != null && socketId >= 0) {
        socketId
    } else {
        val url = call.request.url.toString().toWebSocketUrl()
        val requestHeaders = call.request.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }
        deps.orchestrator.openSocket(
            SocketEntry(
                url = url,
                requestHeaders = requestHeaders,
                status = SocketStatus.Open,
                timestamp = currentTimeMillis(),
            ),
        )
    }
    return WiretapWebSocketSession(this, actualSocketId, deps.orchestrator)
}

/**
 * Wraps a [DefaultClientWebSocketSession] to log all sent and received messages.
 *
 * Automatically detects session completion (timeout, server close, error) and
 * updates the socket status accordingly — no manual `markClosed()`/`markFailed()` needed.
 */
class WiretapWebSocketSession internal constructor(
    val delegate: DefaultClientWebSocketSession,
    private val socketId: Long,
    private val orchestrator: WiretapOrchestrator,
) {

    val incoming get() = delegate.incoming

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
                    orchestrator.updateSocket(
                        SocketEntry(
                            id = socketId,
                            url = url,
                            status = SocketStatus.Failed,
                            failureMessage = cause.message ?: cause::class.simpleName ?: "Unknown error",
                            closedAt = currentTimeMillis(),
                            timestamp = currentTimeMillis(),
                        ),
                    )
                } else {
                    val closeReason = try { delegate.closeReason.getCompleted() } catch (_: Exception) { null }
                    orchestrator.updateSocket(
                        SocketEntry(
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
        when (frame) {
            is Frame.Text -> {
                val text = frame.readText()
                orchestrator.logSocketMsg(
                    SocketMessage(
                        socketId = socketId,
                        direction = SocketMessageDirection.Sent,
                        contentType = SocketContentType.Text,
                        content = text,
                        byteCount = text.encodeToByteArray().size.toLong(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            is Frame.Binary -> {
                val bytes = frame.readBytes()
                orchestrator.logSocketMsg(
                    SocketMessage(
                        socketId = socketId,
                        direction = SocketMessageDirection.Sent,
                        contentType = SocketContentType.Binary,
                        content = "[Binary: ${bytes.size} bytes]",
                        byteCount = bytes.size.toLong(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            else -> { /* pass through */ }
        }
        delegate.send(frame)
    }

    suspend fun logReceivedFrame(frame: Frame) {
        when (frame) {
            is Frame.Text -> {
                val text = frame.readText()
                orchestrator.logSocketMsg(
                    SocketMessage(
                        socketId = socketId,
                        direction = SocketMessageDirection.Received,
                        contentType = SocketContentType.Text,
                        content = text,
                        byteCount = text.encodeToByteArray().size.toLong(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            is Frame.Binary -> {
                val bytes = frame.readBytes()
                orchestrator.logSocketMsg(
                    SocketMessage(
                        socketId = socketId,
                        direction = SocketMessageDirection.Received,
                        contentType = SocketContentType.Binary,
                        content = "[Binary: ${bytes.size} bytes]",
                        byteCount = bytes.size.toLong(),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            else -> { /* ignore Ping/Pong/Close frames for logging */ }
        }
    }

    suspend fun close() {

        statusUpdated = true
        val url = delegate.call.request.url.toString()
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
        val closeReason = try { delegate.closeReason.await() } catch (_: Exception) { null }
        orchestrator.updateSocket(
            SocketEntry(
                id = socketId,
                url = url,
                status = SocketStatus.Closed,
                closeCode = closeReason?.code?.toInt(),
                closeReason = closeReason?.message,
                closedAt = currentTimeMillis(),
                timestamp = currentTimeMillis(),
            ),
        )
        delegate.close()
    }

    suspend fun markFailed(error: String) {

        statusUpdated = true
        val url = delegate.call.request.url.toString()
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
        orchestrator.updateSocket(
            SocketEntry(
                id = socketId,
                url = url,
                status = SocketStatus.Failed,
                failureMessage = error,
                closedAt = currentTimeMillis(),
                timestamp = currentTimeMillis(),
            ),
        )
    }

    suspend fun markClosed(code: Short? = null, reason: String? = null) {

        statusUpdated = true
        val url = delegate.call.request.url.toString()
            .replaceFirst("http://", "ws://")
            .replaceFirst("https://", "wss://")
        orchestrator.updateSocket(
            SocketEntry(
                id = socketId,
                url = url,
                status = SocketStatus.Closed,
                closeCode = code?.toInt(),
                closeReason = reason,
                closedAt = currentTimeMillis(),
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
    val orchestrator: WiretapOrchestrator by inject()
}
