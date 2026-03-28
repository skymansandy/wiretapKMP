package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import kotlinx.coroutines.runBlocking
import okhttp3.WebSocket
import okio.ByteString

/**
 * Wraps an OkHttp WebSocket to intercept outgoing messages for logging.
 */
internal class WiretapWebSocket(
    private val delegate: WebSocket,
    private val socketId: Long,
    private val socketLogManager: SocketLogManager,
) : WebSocket by delegate {

    override fun send(text: String): Boolean {
        runBlocking {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Sent,
                    contentType = SocketContentType.Text,
                    content = text,
                    byteCount = text.encodeToByteArray().size.toLong(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        return delegate.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        runBlocking {
            socketLogManager.logSocketMsg(
                SocketMessage(
                    socketId = socketId,
                    direction = SocketMessageType.Sent,
                    contentType = SocketContentType.Binary,
                    content = "[Binary: ${bytes.size} bytes]",
                    byteCount = bytes.size.toLong(),
                    timestamp = currentTimeMillis(),
                ),
            )
        }
        return delegate.send(bytes)
    }
}
