package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.util.currentTimeMillis
import okhttp3.WebSocket
import okio.ByteString

/**
 * Wraps an OkHttp WebSocket to intercept outgoing messages for logging.
 */
internal class WiretapWebSocket(
    private val delegate: WebSocket,
    private val socketId: Long,
    private val orchestrator: WiretapOrchestrator,
) : WebSocket by delegate {

    override fun send(text: String): Boolean {

        orchestrator.logSocketMessage(
            SocketMessage(
                socketId = socketId,
                direction = SocketMessageDirection.Sent,
                contentType = SocketContentType.Text,
                content = text,
                byteCount = text.encodeToByteArray().size.toLong(),
                timestamp = currentTimeMillis(),
            ),
        )
        return delegate.send(text)
    }

    override fun send(bytes: ByteString): Boolean {

        orchestrator.logSocketMessage(
            SocketMessage(
                socketId = socketId,
                direction = SocketMessageDirection.Sent,
                contentType = SocketContentType.Binary,
                content = "[Binary: ${bytes.size} bytes]",
                byteCount = bytes.size.toLong(),
                timestamp = currentTimeMillis(),
            ),
        )
        return delegate.send(bytes)
    }
}
