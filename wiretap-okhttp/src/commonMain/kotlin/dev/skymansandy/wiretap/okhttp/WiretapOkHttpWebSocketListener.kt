package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Wraps a consumer's WebSocketListener to log all WebSocket events via Wiretap.
 *
 * Usage:
 * ```kotlin
 * val listener = WiretapOkHttpWebSocketListener(myListener)
 * client.newWebSocket(request, listener)
 * ```
 */
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener(), KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val orchestrator: WiretapOrchestrator by inject()
    private var socketId: Long = -1

    override fun onOpen(webSocket: WebSocket, response: Response) = runBlocking {

        val url = webSocket.request().url.toString()
        val reqHeaders = webSocket.request().headers.toMap()

        socketId = orchestrator.openSocket(
            SocketEntry(
                url = url,
                requestHeaders = reqHeaders,
                status = SocketStatus.Open,
                timestamp = currentTimeMillis(),
                protocol = response.protocol.toString(),
            ),
        )

        val wiretapSocket = WiretapWebSocket(webSocket, socketId, orchestrator)
        delegate.onOpen(wiretapSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) = runBlocking {

        if (socketId >= 0) {
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
        delegate.onMessage(webSocket, text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) = runBlocking {

        if (socketId >= 0) {
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
        delegate.onMessage(webSocket, bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) = runBlocking {
        if (socketId >= 0) {
            orchestrator.updateSocket(
                SocketEntry(
                    id = socketId,
                    url = webSocket.request().url.toString(),
                    status = SocketStatus.Closing,
                    closeCode = code,
                    closeReason = reason,
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        delegate.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = runBlocking {
        if (socketId >= 0) {
            orchestrator.updateSocket(
                SocketEntry(
                    id = socketId,
                    url = webSocket.request().url.toString(),
                    status = SocketStatus.Closed,
                    closeCode = code,
                    closeReason = reason,
                    closedAt = currentTimeMillis(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }

        delegate.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) = runBlocking {
        if (socketId >= 0) {
            orchestrator.updateSocket(
                SocketEntry(
                    id = socketId,
                    url = webSocket.request().url.toString(),
                    status = SocketStatus.Failed,
                    failureMessage = t.message ?: t::class.simpleName ?: "Unknown error",
                    closedAt = currentTimeMillis(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        delegate.onFailure(webSocket, t, response)
    }
}
