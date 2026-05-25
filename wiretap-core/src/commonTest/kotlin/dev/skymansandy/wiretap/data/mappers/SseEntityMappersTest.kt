/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.SseEventEntity
import dev.skymansandy.wiretap.data.db.room.entity.SseLogEntity
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SseEntityMappersTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("SseLogEntity to domain") {
        it("copies every scalar field") {
            val entity = SseLogEntity(
                id = 42,
                url = "https://example.com/events",
                requestHeaders = "Accept: text/event-stream",
                status = "Open",
                failureMessage = null,
                eventCount = 5,
                timestamp = 1L,
                closedAt = 2L,
                lastEventId = "evt-123",
                retryMs = 5000L,
                historyCleared = 1L,
            )

            val domain = entity.toDomain()

            domain.id shouldBe 42
            domain.url shouldBe "https://example.com/events"
            domain.requestHeaders shouldBe mapOf("Accept" to "text/event-stream")
            domain.status shouldBe SseStatus.Open
            domain.eventCount shouldBe 5L
            domain.timestamp shouldBe 1L
            domain.closedAt shouldBe 2L
            domain.lastEventId shouldBe "evt-123"
            domain.retryMs shouldBe 5000L
            domain.historyCleared shouldBe true
        }

        it("every SseStatus value round-trips") {
            SseStatus.entries.forEach { status ->
                val entity = baseEntity().copy(status = status.name)
                entity.toDomain().status shouldBe status
            }
        }

        it("historyCleared zero deserializes to false") {
            baseEntity().copy(historyCleared = 0L).toDomain().historyCleared shouldBe false
        }

        it("historyCleared any non-zero deserializes to true") {
            baseEntity().copy(historyCleared = 7L).toDomain().historyCleared shouldBe true
        }
    }

    describe("domain to entity") {
        it("serializes status by name and bool to long") {
            val domain = SseConnection(
                id = 5,
                url = "https://example.com/events",
                status = SseStatus.Failed,
                historyCleared = true,
                timestamp = 1L,
            )

            val entity = domain.toRoomEntity()

            entity.status shouldBe "Failed"
            entity.historyCleared shouldBe 1L
        }
    }

    describe("connection round-trip") {
        it("preserves data") {
            val original = SseConnection(
                id = 12,
                url = "https://example.com/events",
                requestHeaders = mapOf("Accept" to "text/event-stream"),
                status = SseStatus.Closed,
                failureMessage = null,
                eventCount = 3,
                timestamp = 999L,
                closedAt = 1000L,
                lastEventId = "evt-99",
                retryMs = 250L,
                historyCleared = false,
            )

            original.toRoomEntity().toDomain() shouldBe original
        }
    }

    describe("SseEventEntity to domain") {
        it("copies fields including optional ones") {
            val event = SseEventEntity(
                id = 1,
                connectionId = 9,
                eventType = "message",
                data = "{}",
                eventId = "evt-1",
                retryMs = 1000L,
                byteCount = 2,
                timestamp = 1L,
            ).toDomain()

            event.connectionId shouldBe 9L
            event.eventType shouldBe "message"
            event.data shouldBe "{}"
            event.eventId shouldBe "evt-1"
            event.retryMs shouldBe 1000L
            event.byteCount shouldBe 2L
        }

        it("with null optional fields maps to null") {
            val event = SseEventEntity(
                connectionId = 9,
                data = "payload",
                byteCount = 7,
                timestamp = 1L,
            ).toDomain()

            event.eventType shouldBe null
            event.eventId shouldBe null
            event.retryMs shouldBe null
        }
    }
})

private fun baseEntity() = SseLogEntity(
    url = "https://example.com/events",
    requestHeaders = "",
    status = "Connecting",
    timestamp = 0L,
)
