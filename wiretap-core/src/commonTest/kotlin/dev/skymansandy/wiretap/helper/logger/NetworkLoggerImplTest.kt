package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.httpLogEntry
import dev.skymansandy.wiretap.socketLogEntry
import dev.skymansandy.wiretap.socketMessage
import kotlin.test.Test

class NetworkLoggerImplTest {

    private val logger = NetworkLoggerImpl()

    @Test
    fun `logHttp does not throw for in-progress entry`() {
        val entry = httpLogEntry(responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS)
        logger.logHttp(entry)
    }

    @Test
    fun `logHttp does not throw for completed entry`() {
        val entry = httpLogEntry(responseCode = 200, durationMs = 150)
        logger.logHttp(entry)
    }

    @Test
    fun `logHttp does not throw with durationNs`() {
        val entry = httpLogEntry(responseCode = 200, durationNs = 1_500_000L)
        logger.logHttp(entry)
    }

    @Test
    fun `logHttp does not throw with protocol and remote address`() {
        val entry = httpLogEntry(
            responseCode = 200,
            protocol = "HTTP/2",
            remoteAddress = "10.0.0.1",
        )
        logger.logHttp(entry)
    }

    @Test
    fun `logHttp does not throw for mock source`() {
        val entry = httpLogEntry(responseCode = 200, source = ResponseSource.Mock)
        logger.logHttp(entry)
    }

    @Test
    fun `logSocket does not throw for each status`() {
        SocketStatus.entries.forEach { status ->
            val entry = socketLogEntry(
                status = status,
                closeCode = if (status == SocketStatus.Closed) 1000 else null,
                closeReason = if (status == SocketStatus.Closed) "Normal" else null,
                failureMessage = if (status == SocketStatus.Failed) "Connection refused" else null,
            )
            logger.logSocket(entry)
        }
    }

    @Test
    fun `logSocketMessage does not throw for sent message`() {
        val message = socketMessage(direction = SocketMessageDirection.Sent)
        logger.logSocketMessage(message)
    }

    @Test
    fun `logSocketMessage does not throw for received message`() {
        val message = socketMessage(direction = SocketMessageDirection.Received)
        logger.logSocketMessage(message)
    }

    @Test
    fun `logSocketMessage does not throw for long content truncation`() {
        val longContent = "a".repeat(200)
        val message = socketMessage(content = longContent, byteCount = 200)
        logger.logSocketMessage(message)
    }

    @Test
    fun `logSocketMessage does not throw for binary content type`() {
        val message = socketMessage(contentType = SocketContentType.Binary)
        logger.logSocketMessage(message)
    }

    @Test
    fun `logSocketMessage does not throw for large byte counts`() {
        val message = socketMessage(byteCount = 2_097_152L) // 2 MB
        logger.logSocketMessage(message)
    }
}
