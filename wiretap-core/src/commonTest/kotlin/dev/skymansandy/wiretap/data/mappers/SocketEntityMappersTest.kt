/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.mappers

import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SocketEntityMappersTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("SocketLogEntity to domain") {
        it("copies every scalar field") {
            val entity = SocketLogEntity(
                id = 99,
                url = "wss://example.com/socket",
                requestHeaders = "Authorization: Bearer abc",
                status = "Open",
                closeCode = 1000L,
                closeReason = "Normal",
                failureMessage = null,
                messageCount = 17,
                timestamp = 1L,
                closedAt = 2L,
                protocol = "wss",
                remoteAddress = "1.2.3.4:443",
                historyCleared = 1L,
            )

            val domain = entity.toDomain()

            domain.id shouldBe 99
            domain.url shouldBe "wss://example.com/socket"
            domain.requestHeaders shouldBe mapOf("Authorization" to "Bearer abc")
            domain.status shouldBe SocketStatus.Open
            domain.closeCode shouldBe 1000
            domain.closeReason shouldBe "Normal"
            domain.failureMessage shouldBe null
            domain.messageCount shouldBe 17L
            domain.timestamp shouldBe 1L
            domain.closedAt shouldBe 2L
            domain.protocol shouldBe "wss"
            domain.remoteAddress shouldBe "1.2.3.4:443"
            domain.historyCleared shouldBe true
        }

        it("every SocketStatus value round-trips through the entity") {
            SocketStatus.entries.forEach { status ->
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

        it("null closeCode in entity round-trips as null in domain") {
            baseEntity().copy(closeCode = null).toDomain().closeCode shouldBe null
        }
    }

    describe("domain to entity") {
        it("serializes status by name and bool to long") {
            val domain = SocketConnection(
                id = 5,
                url = "wss://example.com",
                status = SocketStatus.Failed,
                historyCleared = true,
                timestamp = 1L,
            )

            val entity = domain.toRoomEntity()

            entity.status shouldBe "Failed"
            entity.historyCleared shouldBe 1L
        }

        it("with false historyCleared serializes to zero") {
            val entity = SocketConnection(url = "x", timestamp = 0L, historyCleared = false).toRoomEntity()
            entity.historyCleared shouldBe 0L
        }
    }

    describe("socket connection round-trip") {
        it("preserves data") {
            val original = SocketConnection(
                id = 12,
                url = "wss://example.com",
                requestHeaders = mapOf("Accept" to "*/*"),
                status = SocketStatus.Closed,
                closeCode = 1001,
                closeReason = "going away",
                failureMessage = null,
                messageCount = 4,
                timestamp = 999L,
                closedAt = 1000L,
                protocol = "wss",
                remoteAddress = "1.2.3.4:443",
                historyCleared = false,
            )

            val roundTripped = original.toRoomEntity().toDomain()

            // statusColor is computed in the class body, not a ctor param — data class
            // equality covers all ctor-declared fields.
            roundTripped shouldBe original
        }
    }

    describe("SocketMessageEntity to domain") {
        it("maps direction and content type by name") {
            SocketMessageType.entries.forEach { direction ->
                SocketContentType.entries.forEach { contentType ->
                    val message = SocketMessageEntity(
                        id = 1,
                        socketId = 9,
                        direction = direction.name,
                        contentType = contentType.name,
                        content = "payload",
                        byteCount = 7,
                        timestamp = 1L,
                    ).toDomain()

                    message.socketId shouldBe 9L
                    message.direction shouldBe direction
                    message.contentType shouldBe contentType
                    message.content shouldBe "payload"
                    message.byteCount shouldBe 7L
                }
            }
        }
    }
})

private fun baseEntity() = SocketLogEntity(
    id = 1,
    url = "wss://example.com",
    requestHeaders = "",
    status = "Connecting",
    timestamp = 0L,
)
