package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.room.dao.SocketRoomDao
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.socketLogEntry
import dev.skymansandy.wiretap.socketMessage
import dev.skymansandy.wiretap.toRoomEntity
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SocketRepositoryImplTest {

    private lateinit var socketRoomDao: SocketRoomDao
    private lateinit var repository: SocketRepositoryImpl

    @BeforeTest
    fun setup() {
        socketRoomDao = mock<SocketRoomDao>()
        repository = SocketRepositoryImpl(socketRoomDao)
    }

    @Test
    fun `openConnection returns id from dao`() = runTest {
        everySuspend { socketRoomDao.insertSocketLog(any<SocketLogEntity>()) } returns 10L

        val id = repository.openConnection(socketLogEntry())

        id shouldBe 10L
    }

    @Test
    fun `reopenConnection delegates to dao`() = runTest {
        everySuspend { socketRoomDao.insertSocketLogWithId(any<SocketLogEntity>()) } returns Unit

        repository.reopenConnection(socketLogEntry(id = 10))

        verifySuspend { socketRoomDao.insertSocketLogWithId(any<SocketLogEntity>()) }
    }

    @Test
    fun `updateConnection delegates to dao`() = runTest {
        everySuspend {
            socketRoomDao.updateSocketLog(
                status = any(),
                closeCode = any(),
                closeReason = any(),
                failureMessage = any(),
                closedAt = any(),
                protocol = any(),
                remoteAddress = any(),
                id = any(),
            )
        } returns Unit

        repository.updateConnection(socketLogEntry(id = 10))

        verifySuspend {
            socketRoomDao.updateSocketLog(
                status = any(),
                closeCode = any(),
                closeReason = any(),
                failureMessage = any(),
                closedAt = any(),
                protocol = any(),
                remoteAddress = any(),
                id = any(),
            )
        }
    }

    @Test
    fun `logMessage inserts message and increments count`() = runTest {
        everySuspend { socketRoomDao.insertSocketMessage(any<SocketMessageEntity>()) } returns Unit
        everySuspend { socketRoomDao.incrementSocketMessageCount(10L) } returns Unit

        repository.logMessage(socketMessage(socketId = 10))

        verifySuspend { socketRoomDao.insertSocketMessage(any<SocketMessageEntity>()) }
        verifySuspend { socketRoomDao.incrementSocketMessageCount(10L) }
    }

    @Test
    fun `getById returns entry from dao`() = runTest {
        everySuspend { socketRoomDao.getSocketLogById(10L) } returns socketLogEntry(id = 10).toRoomEntity()

        val result = repository.getById(10L)
        result?.id shouldBe 10L
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { socketRoomDao.getSocketLogById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `getMessages returns flow from dao`() = runTest {
        val roomMessages = listOf(socketMessage(id = 1).toRoomEntity(), socketMessage(id = 2).toRoomEntity())
        every { socketRoomDao.getSocketMessagesBySocketId(10L) } returns flowOf(roomMessages)

        repository.getMessages(10L).test {
            val items = awaitItem()
            items[0].id shouldBe 1L
            items[1].id shouldBe 2L
            awaitComplete()
        }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val roomEntities = listOf(socketLogEntry(id = 1).toRoomEntity(), socketLogEntry(id = 2).toRoomEntity())
        every { socketRoomDao.getAllSocketLogs() } returns flowOf(roomEntities)

        repository.getAll().test {
            val items = awaitItem()
            items[0].id shouldBe 1L
            items[1].id shouldBe 2L
            awaitComplete()
        }
    }

    @Test
    fun `clearAll delegates to dao`() = runTest {
        everySuspend { socketRoomDao.deleteAllSocketMessages() } returns Unit
        everySuspend { socketRoomDao.deleteAllSocketLogs() } returns Unit

        repository.clearAll()

        verifySuspend { socketRoomDao.deleteAllSocketMessages() }
        verifySuspend { socketRoomDao.deleteAllSocketLogs() }
    }

    @Test
    fun `clearClosed delegates to dao`() = runTest {
        everySuspend { socketRoomDao.deleteClosedSocketMessages() } returns Unit
        everySuspend { socketRoomDao.deleteClosedSocketLogs() } returns Unit

        repository.clearClosed()

        verifySuspend { socketRoomDao.deleteClosedSocketMessages() }
        verifySuspend { socketRoomDao.deleteClosedSocketLogs() }
    }

    @Test
    fun `getByIdFlow emits initial value and updates on invalidation`() = runTest {
        everySuspend { socketRoomDao.getSocketLogById(10L) } returns socketLogEntry(id = 10).toRoomEntity()
        everySuspend {
            socketRoomDao.updateSocketLog(
                status = any(),
                closeCode = any(),
                closeReason = any(),
                failureMessage = any(),
                closedAt = any(),
                protocol = any(),
                remoteAddress = any(),
                id = any(),
            )
        } returns Unit

        repository.getByIdFlow(10L).test {
            awaitItem()?.id shouldBe 10L

            repository.updateConnection(socketLogEntry(id = 10))

            awaitItem()?.id shouldBe 10L
            cancelAndIgnoreRemainingEvents()
        }
    }
}
