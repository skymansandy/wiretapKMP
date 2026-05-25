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
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class WiretapLoggerImplTest : DescribeSpec({
    val logger = WiretapLoggerImpl()

    describe("logHttp") {
        it("prints ellipsis line and skips duration metadata for in-progress entry") {
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

        it("uses ms duration when durationNs is zero") {
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

        it("uses ns duration formatted when present") {
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

            output shouldContain "12.345ms"
            output shouldContain "[Mock]"
        }

        describe("formatNs") {
            it("handles nanoseconds branch") {
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

            it("handles microseconds branch") {
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

            it("handles seconds branch") {
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
        }

        it("appends protocol and remoteAddress when present") {
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

        it("omits protocol and remoteAddress when null") {
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
    }

    describe("logSocket") {
        it("emits CONNECTING line with url for Connecting status") {
            val output = captureStdout { logger.logSocket(socketWith(SocketStatus.Connecting)) }
            output shouldContain "WS CONNECTING wss://x"
        }

        it("emits OPEN line for Open status") {
            val output = captureStdout { logger.logSocket(socketWith(SocketStatus.Open)) }
            output shouldContain "WS OPEN wss://x"
        }

        it("emits CLOSING line for Closing status") {
            val output = captureStdout { logger.logSocket(socketWith(SocketStatus.Closing)) }
            output shouldContain "WS CLOSING wss://x"
        }

        it("emits CLOSED line with close code and reason for Closed status") {
            val output = captureStdout {
                logger.logSocket(
                    socketWith(SocketStatus.Closed, closeCode = 1000, closeReason = "normal"),
                )
            }
            output shouldContain "WS CLOSED 1000 \"normal\""
        }

        it("emits empty quoted reason when Closed has null reason") {
            val output = captureStdout {
                logger.logSocket(socketWith(SocketStatus.Closed, closeCode = 1000, closeReason = null))
            }
            output shouldContain "WS CLOSED 1000 \"\""
        }

        it("includes failure message for Failed status") {
            val output = captureStdout {
                logger.logSocket(socketWith(SocketStatus.Failed, failureMessage = "boom"))
            }
            output shouldContain "WS FAILED wss://x boom"
        }
    }

    describe("logSocketMessage") {
        it("uses up arrow for Sent message") {
            val output = captureStdout {
                logger.logSocketMessage(socketMessage(direction = SocketMessageType.Sent, content = "ping"))
            }
            output shouldContain "WS ▲ \"ping\""
        }

        it("uses down arrow for Received message") {
            val output = captureStdout {
                logger.logSocketMessage(socketMessage(direction = SocketMessageType.Received, content = "pong"))
            }
            output shouldContain "WS ▼ \"pong\""
        }

        it("truncates messages longer than 80 chars with ellipsis") {
            val content = "x".repeat(100)
            val output = captureStdout {
                logger.logSocketMessage(socketMessage(content = content))
            }
            output shouldContain "\"" + "x".repeat(80) + "...\""
        }

        it("renders byteCount with correct unit") {
            val output = captureStdout {
                logger.logSocketMessage(socketMessage(byteCount = 2_048))
            }
            output shouldContain "(2 kB)"
        }
    }

    describe("SSE logger defaults") {
        it("are no-op smoke") {
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
})

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
