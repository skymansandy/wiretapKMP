/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLog.Companion.RESPONSE_CODE_IN_PROGRESS
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test

class WiretapLoggerImplTest {

    private val logger = WiretapLoggerImpl()

    private fun captureStdout(block: () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer))
        try {
            block()
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }

    // ---- logHttp ------------------------------------------------------------

    @Test
    fun `in-progress entry prints ellipsis line and skips duration metadata`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "POST",
                    responseCode = RESPONSE_CODE_IN_PROGRESS,
                    timestamp = 0,
                ),
            )
        }

        output shouldContain "POST https://x -> ..."
        output shouldNotContain "[Network]"
        output shouldNotContain "(0ms)"
    }

    @Test
    fun `completed entry uses ms duration when durationNs is zero`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationMs = 42,
                    durationNs = 0,
                    timestamp = 0,
                    source = ResponseSource.Network,
                ),
            )
        }

        output shouldContain "GET https://x -> 200 (42ms) [Network]"
    }

    @Test
    fun `completed entry uses ns duration formatted when present`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationMs = 12,
                    durationNs = 12_345_678,
                    timestamp = 0,
                    source = ResponseSource.Mock,
                ),
            )
        }

        // 12_345_678 ns -> 12.345ms via integer math in formatNs
        output shouldContain "12.345ms"
        output shouldContain "[Mock]"
    }

    @Test
    fun `formatNs handles nanoseconds branch`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationNs = 500,
                    timestamp = 0,
                ),
            )
        }
        output shouldContain "(500ns)"
    }

    @Test
    fun `formatNs handles microseconds branch`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationNs = 1_500,
                    timestamp = 0,
                ),
            )
        }
        output shouldContain "(1.500µs)"
    }

    @Test
    fun `formatNs handles seconds branch`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationNs = 2_500_000_000,
                    timestamp = 0,
                ),
            )
        }
        output shouldContain "(2.500s)"
    }

    @Test
    fun `protocol and remoteAddress are appended when present`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationMs = 1,
                    timestamp = 0,
                    protocol = "HTTP/2",
                    remoteAddress = "1.2.3.4:443",
                ),
            )
        }

        output shouldContain "[Network] HTTP/2 @1.2.3.4:443"
    }

    @Test
    fun `protocol and remoteAddress are omitted when null`() {
        val output = captureStdout {
            logger.logHttp(
                HttpLog(
                    url = "https://x",
                    method = "GET",
                    responseCode = 200,
                    durationMs = 1,
                    timestamp = 0,
                ),
            )
        }

        output shouldNotContain "HTTP/"
        output shouldNotContain "@"
    }

    // ---- logSocket ---------------------------------------------------------

    @Test
    fun `Connecting status emits CONNECTING line with url`() {
        val output = captureStdout {
            logger.logSocket(socketWith(SocketStatus.Connecting))
        }

        output shouldContain "WS CONNECTING wss://x"
    }

    @Test
    fun `Open status emits OPEN line`() {
        val output = captureStdout { logger.logSocket(socketWith(SocketStatus.Open)) }
        output shouldContain "WS OPEN wss://x"
    }

    @Test
    fun `Closing status emits CLOSING line`() {
        val output = captureStdout { logger.logSocket(socketWith(SocketStatus.Closing)) }
        output shouldContain "WS CLOSING wss://x"
    }

    @Test
    fun `Closed status emits CLOSED line with close code and reason`() {
        val output = captureStdout {
            logger.logSocket(
                socketWith(SocketStatus.Closed, closeCode = 1000, closeReason = "normal"),
            )
        }
        output shouldContain "WS CLOSED 1000 \"normal\""
    }

    @Test
    fun `Closed status with null reason emits empty quoted reason`() {
        val output = captureStdout {
            logger.logSocket(socketWith(SocketStatus.Closed, closeCode = 1000, closeReason = null))
        }
        output shouldContain "WS CLOSED 1000 \"\""
    }

    @Test
    fun `Failed status includes failure message`() {
        val output = captureStdout {
            logger.logSocket(socketWith(SocketStatus.Failed, failureMessage = "boom"))
        }
        output shouldContain "WS FAILED wss://x boom"
    }

    // ---- logSocketMessage --------------------------------------------------

    @Test
    fun `Sent message uses up arrow`() {
        val output = captureStdout {
            logger.logSocketMessage(socketMessage(direction = SocketMessageType.Sent, content = "ping"))
        }
        output shouldContain "WS ▲ \"ping\""
    }

    @Test
    fun `Received message uses down arrow`() {
        val output = captureStdout {
            logger.logSocketMessage(socketMessage(direction = SocketMessageType.Received, content = "pong"))
        }
        output shouldContain "WS ▼ \"pong\""
    }

    @Test
    fun `message longer than 80 chars is truncated with ellipsis`() {
        val content = "x".repeat(100)
        val output = captureStdout {
            logger.logSocketMessage(socketMessage(content = content))
        }
        output shouldContain "\"" + "x".repeat(80) + "...\""
    }

    @Test
    fun `byteCount is rendered with correct unit`() {
        val output = captureStdout {
            logger.logSocketMessage(socketMessage(byteCount = 2_048))
        }
        output shouldContain "(2 kB)"
    }

    // ---- helpers -----------------------------------------------------------

    private fun socketWith(
        status: SocketStatus,
        closeCode: Int? = null,
        closeReason: String? = null,
        failureMessage: String? = null,
    ) = SocketConnection(
        url = "wss://x",
        status = status,
        closeCode = closeCode,
        closeReason = closeReason,
        failureMessage = failureMessage,
        timestamp = 0,
    )

    private fun socketMessage(
        direction: SocketMessageType = SocketMessageType.Sent,
        content: String = "hi",
        byteCount: Long = 2,
    ) = SocketMessage(
        socketId = 1,
        direction = direction,
        contentType = SocketContentType.Text,
        content = content,
        byteCount = byteCount,
        timestamp = 0,
    )

    @Test
    fun `default Sse functions are no-op (only smoke)`() {
        // logger has default no-op impls for SSE
        logger.logSse(dev.skymansandy.wiretap.domain.model.SseConnection(url = "x", timestamp = 0))
        logger.logSseEvent(
            dev.skymansandy.wiretap.domain.model.SseEvent(
                connectionId = 0,
                data = "x",
                byteCount = 1,
                timestamp = 0,
            ),
        )
    }
}
