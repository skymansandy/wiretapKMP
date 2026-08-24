/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SseLogUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("buildSseShareText") {
        it("starts with SSE url and status") {
            val text = buildSseShareText(connection(status = SseStatus.Open), emptyList())

            val lines = text.lines()
            lines[0] shouldBe "SSE https://example.com/stream"
            lines[1] shouldBe "Status: Open"
            lines[2].startsWith("Opened: ") shouldBe true
        }

        it("omits optional connection metadata when null") {
            val text = buildSseShareText(connection(), emptyList())

            text shouldNotContain "Closed:"
            text shouldNotContain "Error:"
            text shouldNotContain "Last Event ID:"
            text shouldNotContain "Retry:"
        }

        it("emits closure, error, last-id and retry when populated") {
            val text = buildSseShareText(
                connection(
                    closedAt = 1L,
                    failureMessage = "boom",
                    lastEventId = "evt-42",
                    retryMs = 5000L,
                ),
                emptyList(),
            )

            text shouldContain "Error: boom"
            text shouldContain "Last Event ID: evt-42"
            text shouldContain "Retry: 5000ms"
        }

        it("prints none placeholder for empty request headers and events") {
            val text = buildSseShareText(connection(), emptyList())

            text shouldContain "--- Request Headers ---\n(none)"
            text shouldContain "--- Events (0) ---\n(none)"
        }

        it("lists request headers as key colon value lines") {
            val text = buildSseShareText(
                connection(requestHeaders = mapOf("Accept" to "text/event-stream")),
                emptyList(),
            )

            text shouldContain "Accept: text/event-stream"
        }

        it("renders an event block with event-type id and byte count then data") {
            val text = buildSseShareText(
                connection(),
                listOf(
                    event(
                        eventType = "message",
                        eventId = "1",
                        data = """{"hello":"world"}""",
                        byteCount = 17L,
                    ),
                ),
            )

            text shouldContain "event: message"
            text shouldContain "id: 1"
            text shouldContain "(17 B)"
            text shouldContain """{"hello":"world"}"""
        }

        it("omits event-type and id when not set") {
            val text = buildSseShareText(
                connection(),
                listOf(event(eventType = null, eventId = null, data = "raw")),
            )

            text shouldNotContain "event:"
            text shouldNotContain "id:"
            text shouldContain "raw"
        }

        it("reports the total event count in the events section header") {
            val text = buildSseShareText(
                connection(),
                listOf(event(), event(), event()),
            )

            text shouldContain "--- Events (3) ---"
        }
    }
})

@Suppress("LongParameterList")
private fun connection(
    url: String = "https://example.com/stream",
    requestHeaders: Map<String, String> = emptyMap(),
    status: SseStatus = SseStatus.Connecting,
    closedAt: Long? = null,
    failureMessage: String? = null,
    lastEventId: String? = null,
    retryMs: Long? = null,
) = SseConnection(
    url = url,
    requestHeaders = requestHeaders,
    status = status,
    failureMessage = failureMessage,
    timestamp = 0L,
    closedAt = closedAt,
    lastEventId = lastEventId,
    retryMs = retryMs,
)

private fun event(
    eventType: String? = null,
    data: String = "{}",
    eventId: String? = null,
    byteCount: Long = 2L,
) = SseEvent(
    connectionId = 0L,
    eventType = eventType,
    data = data,
    eventId = eventId,
    byteCount = byteCount,
    timestamp = 0L,
)
