/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SocketLogUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("buildSocketShareText") {
        it("starts with WS url and status") {
            val text = buildSocketShareText(connection(status = SocketStatus.Open), emptyList())

            val lines = text.lines()
            lines[0] shouldBe "WS wss://example.com/chat"
            lines[1] shouldBe "Status: Open"
            lines[2].startsWith("Opened: ") shouldBe true
        }

        it("omits optional connection metadata when null") {
            val text = buildSocketShareText(connection(), emptyList())

            text shouldNotContain "Closed:"
            text shouldNotContain "Close Code:"
            text shouldNotContain "Close Reason:"
            text shouldNotContain "Error:"
            text shouldNotContain "Protocol:"
            text shouldNotContain "Remote Address:"
        }

        it("emits closure and protocol lines when populated") {
            val text = buildSocketShareText(
                connection(
                    closedAt = 1L,
                    closeCode = 1000,
                    closeReason = "Normal",
                    failureMessage = "boom",
                    protocol = "chat",
                    remoteAddress = "1.2.3.4:443",
                ),
                emptyList(),
            )

            text shouldContain "Close Code: 1000"
            text shouldContain "Close Reason: Normal"
            text shouldContain "Error: boom"
            text shouldContain "Protocol: chat"
            text shouldContain "Remote Address: 1.2.3.4:443"
        }

        it("prints none placeholder for empty request headers and messages") {
            val text = buildSocketShareText(
                connection(requestHeaders = emptyMap()),
                emptyList(),
            )

            text shouldContain "--- Request Headers ---\n(none)"
            text shouldContain "--- Messages (0) ---\n(none)"
        }

        it("lists request headers as key colon value lines") {
            val text = buildSocketShareText(
                connection(requestHeaders = mapOf("Sec-WebSocket-Key" to "abc==")),
                emptyList(),
            )

            text shouldContain "Sec-WebSocket-Key: abc=="
        }

        it("formats text sent and received messages with direction arrows") {
            val text = buildSocketShareText(
                connection(),
                listOf(
                    message(direction = SocketMessageType.Sent, content = """{"ping":1}""", byteCount = 10),
                    message(direction = SocketMessageType.Received, content = """{"pong":1}""", byteCount = 10),
                ),
            )

            text shouldContain """>> SENT [Text, 10 B] {"ping":1}"""
            text shouldContain """<< RECV [Text, 10 B] {"pong":1}"""
        }

        it("formats binary messages with the Binary content type tag") {
            val text = buildSocketShareText(
                connection(),
                listOf(
                    message(
                        direction = SocketMessageType.Received,
                        contentType = SocketContentType.Binary,
                        content = "[Binary: 1.0 KB]",
                        byteCount = 1024,
                    ),
                ),
            )

            text shouldContain "<< RECV [Binary,"
            text shouldContain "[Binary: 1.0 KB]"
        }

        it("formats control frames as dash dash markers") {
            val text = buildSocketShareText(
                connection(),
                listOf(
                    message(contentType = SocketContentType.Ping, content = ""),
                    message(contentType = SocketContentType.Pong, content = ""),
                    message(contentType = SocketContentType.Close, content = "going away"),
                ),
            )

            text shouldContain "-- PING"
            text shouldContain "-- PONG"
            text shouldContain "-- CLOSE — going away"
        }

        it("reports the total message count in the messages section header") {
            val text = buildSocketShareText(
                connection(),
                listOf(
                    message(),
                    message(),
                    message(),
                ),
            )

            text shouldContain "--- Messages (3) ---"
        }
    }
})

@Suppress("LongParameterList")
private fun connection(
    url: String = "wss://example.com/chat",
    requestHeaders: Map<String, String> = emptyMap(),
    status: SocketStatus = SocketStatus.Connecting,
    closedAt: Long? = null,
    closeCode: Int? = null,
    closeReason: String? = null,
    failureMessage: String? = null,
    protocol: String? = null,
    remoteAddress: String? = null,
) = SocketConnection(
    url = url,
    requestHeaders = requestHeaders,
    status = status,
    closeCode = closeCode,
    closeReason = closeReason,
    failureMessage = failureMessage,
    timestamp = 0L,
    closedAt = closedAt,
    protocol = protocol,
    remoteAddress = remoteAddress,
)

private fun message(
    direction: SocketMessageType = SocketMessageType.Sent,
    contentType: SocketContentType = SocketContentType.Text,
    content: String = "{}",
    byteCount: Long = 2L,
) = SocketMessage(
    socketId = 0L,
    direction = direction,
    contentType = contentType,
    content = content,
    byteCount = byteCount,
    timestamp = 0L,
)
