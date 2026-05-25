/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.SocketLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.domain.model.SocketStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SocketRepositoryImplTest {

    private val dao = mock<SocketLogsDao>(MockMode.autoUnit)
    private val repo = SocketRepositoryImpl(dao)

    @Test
    fun `flowAll maps entities to domain`() = runTest {
        every { dao.getAllSocketLogs() } returns flowOf(listOf(socketEntity(1)))

        repo.flowAll().test {
            awaitItem().first().id shouldBe 1L
            awaitComplete()
        }
    }

    @Test
    fun `flowMessagesForId maps every entity returned by the dao`() = runTest {
        every { dao.getSocketMessagesBySocketId(7) } returns flowOf(
            listOf(messageEntity(socketId = 7, content = "hi")),
        )

        repo.flowMessagesForId(7).test {
            val msgs = awaitItem()
            msgs.size shouldBe 1
            msgs[0].socketId shouldBe 7L
            msgs[0].content shouldBe "hi"
            awaitComplete()
        }
    }

    @Test
    fun `flowById emits on subscription and maps to domain`() = runTest {
        everySuspend { dao.getSocketLogById(5) } returns socketEntity(5)

        repo.flowById(5).test {
            awaitItem()?.id shouldBe 5L
            // The flow stays open observing the invalidation signal — cancel manually.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getById returns null when dao returns null`() = runTest {
        everySuspend { dao.getSocketLogById(99) } returns null
        repo.getById(99) shouldBe null
    }

    @Test
    fun `logNew returns the id from the dao`() = runTest {
        everySuspend { dao.insertSocketLog(any()) } returns 77L

        val id = repo.logNew(socketConnection(url = "wss://x"))

        id shouldBe 77L
        verifySuspend { dao.insertSocketLog(any()) }
    }

    @Test
    fun `markReopened upserts the entity via insertSocketLogWithId`() = runTest {
        repo.markReopened(socketConnection(url = "wss://x"))
        verifySuspend { dao.insertSocketLogWithId(any()) }
    }

    @Test
    fun `update passes the populated fields to the dao update query`() = runTest {
        val connection = SocketConnection(
            id = 5,
            url = "wss://x",
            status = SocketStatus.Closed,
            closeCode = 1000,
            closeReason = "ok",
            failureMessage = null,
            closedAt = 99L,
            protocol = "wss",
            remoteAddress = "1.2.3.4",
            timestamp = 0,
        )

        repo.update(connection)

        verifySuspend {
            dao.updateSocketLog(
                status = "Closed",
                closeCode = 1000L,
                closeReason = "ok",
                failureMessage = null,
                closedAt = 99L,
                protocol = "wss",
                remoteAddress = "1.2.3.4",
                id = 5,
            )
        }
    }

    @Test
    fun `logSocketMsg inserts the message and increments the count`() = runTest {
        val message = SocketMessage(
            socketId = 9,
            direction = SocketMessageType.Sent,
            contentType = SocketContentType.Text,
            content = "ping",
            byteCount = 4,
            timestamp = 0,
        )

        repo.logSocketMsg(message)

        verifySuspend {
            dao.insertSocketMessage(
                matches<SocketMessageEntity> {
                    it.socketId == 9L &&
                        it.direction == "Sent" &&
                        it.contentType == "Text" &&
                        it.content == "ping" &&
                        it.byteCount == 4L
                },
            )
        }
        verifySuspend { dao.incrementSocketMessageCount(9) }
    }

    @Test
    fun `clearAll deletes messages and logs`() = runTest {
        repo.clearAll()
        verifySuspend { dao.deleteAllSocketMessages() }
        verifySuspend { dao.deleteAllSocketLogs() }
    }

    @Test
    fun `clearClosed deletes closed messages and logs`() = runTest {
        repo.clearClosed()
        verifySuspend { dao.deleteClosedSocketMessages() }
        verifySuspend { dao.deleteClosedSocketLogs() }
    }

    // ---- helpers -----------------------------------------------------------

    private fun socketConnection(url: String) = SocketConnection(
        url = url,
        status = SocketStatus.Open,
        timestamp = 0,
    )

    private fun socketEntity(id: Long) = SocketLogEntity(
        id = id,
        url = "wss://x",
        requestHeaders = "",
        status = "Open",
        timestamp = 0,
    )

    private fun messageEntity(socketId: Long, content: String) = SocketMessageEntity(
        socketId = socketId,
        direction = "Sent",
        contentType = "Text",
        content = content,
        byteCount = content.length.toLong(),
        timestamp = 0,
    )
}
