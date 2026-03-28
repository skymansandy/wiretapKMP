package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
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
 * Usage — extension function:
 * ```kotlin
 * client.newWebSocket(request, myListener.wiretapped())
 * ```
 *
 * Usage — constructor:
 * ```kotlin
 * val listener = WiretapOkHttpWebSocketListener(myListener)
 * client.newWebSocket(request, listener)
 * ```
 */
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener(), KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val socketLogManager: SocketLogManager by inject()

    private var socketId: Long = -1
    private val isSocketActive
        get() = socketId >= 0

    override fun onOpen(webSocket: WebSocket, response: Response) = runBlocking {
        val url = webSocket.request().url.toString()
        val reqHeaders = webSocket.request().headers.toMap()

        socketId = socketLogManager.createSocket(
            SocketConnection(
                url = url,
                requestHeaders = reqHeaders,
                status = SocketStatus.Open,
                timestamp = currentTimeMillis(),
                protocol = response.protocol.toString(),
            ),
        )

        val wiretapSocket = WiretapWebSocket(webSocket, socketId, socketLogManager)
        delegate.onOpen(wiretapSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) = runBlocking {
        if (isSocketActive) {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Received,
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
        if (isSocketActive) {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Received,
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
        if (isSocketActive) {
            socketLogManager.updateSocket(
                SocketConnection(
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
        if (isSocketActive) {
            socketLogManager.updateSocket(
                SocketConnection(
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
        if (isSocketActive) {
            socketLogManager.updateSocket(
                SocketConnection(
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

/**
 * Wraps this [WebSocketListener] with Wiretap logging.
 *
 * ```kotlin
 * client.newWebSocket(request, myListener.wiretapped())
 * ```
 */
fun WebSocketListener.wiretapped(): WiretapOkHttpWebSocketListener =
    WiretapOkHttpWebSocketListener(this)
