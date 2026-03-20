package dev.skymansandy.wiretap.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.socketLogEntry
import dev.skymansandy.wiretap.socketMessage
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SocketRepositoryImplTest {

    private lateinit var socketDao: SocketDao
    private lateinit var repository: SocketRepositoryImpl

    @BeforeTest
    fun setup() {
        socketDao = mock<SocketDao>()
        repository = SocketRepositoryImpl(socketDao)
    }

    @Test
    fun `openConnection returns id from dao`() = runTest {
        val entry = socketLogEntry()
        everySuspend { socketDao.insertAndGetId(entry) } returns 10L

        val id = repository.openConnection(entry)

        id shouldBe 10L
    }

    @Test
    fun `reopenConnection delegates to dao`() = runTest {
        val entry = socketLogEntry(id = 10)
        everySuspend { socketDao.insertWithId(entry) } returns Unit

        repository.reopenConnection(entry)

        verifySuspend { socketDao.insertWithId(entry) }
    }

    @Test
    fun `updateConnection delegates to dao`() = runTest {
        val entry = socketLogEntry(id = 10)
        everySuspend { socketDao.update(entry) } returns Unit

        repository.updateConnection(entry)

        verifySuspend { socketDao.update(entry) }
    }

    @Test
    fun `logMessage inserts message and increments count`() = runTest {
        val message = socketMessage(socketId = 10)
        everySuspend { socketDao.insertMessage(message) } returns Unit
        everySuspend { socketDao.incrementMessageCount(10L) } returns Unit

        repository.logMessage(message)

        verifySuspend { socketDao.insertMessage(message) }
        verifySuspend { socketDao.incrementMessageCount(10L) }
    }

    @Test
    fun `getById returns entry from dao`() = runTest {
        val entry = socketLogEntry(id = 10)
        everySuspend { socketDao.getById(10L) } returns entry

        repository.getById(10L) shouldBe entry
    }

    @Test
    fun `getById returns null when not found`() = runTest {
        everySuspend { socketDao.getById(999L) } returns null

        repository.getById(999L).shouldBeNull()
    }

    @Test
    fun `getMessages returns flow from dao`() = runTest {
        val messages = listOf(socketMessage(id = 1), socketMessage(id = 2))
        every { socketDao.getMessages(10L) } returns flowOf(messages)

        repository.getMessages(10L).test {
            awaitItem() shouldBe messages
            awaitComplete()
        }
    }

    @Test
    fun `getAll returns flow from dao`() = runTest {
        val entries = listOf(socketLogEntry(id = 1), socketLogEntry(id = 2))
        every { socketDao.getAll() } returns flowOf(entries)

        repository.getAll().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `clearAll delegates to dao`() = runTest {
        everySuspend { socketDao.deleteAll() } returns Unit

        repository.clearAll()

        verifySuspend { socketDao.deleteAll() }
    }

    @Test
    fun `clearClosed delegates to dao`() = runTest {
        everySuspend { socketDao.deleteClosed() } returns Unit

        repository.clearClosed()

        verifySuspend { socketDao.deleteClosed() }
    }

    @Test
    fun `getByIdFlow emits initial value and updates on invalidation`() = runTest {
        val entry = socketLogEntry(id = 10)
        everySuspend { socketDao.getById(10L) } returns entry

        repository.getByIdFlow(10L).test {
            awaitItem() shouldBe entry

            // Trigger invalidation by performing a write
            everySuspend { socketDao.update(entry) } returns Unit
            repository.updateConnection(entry)

            awaitItem() shouldBe entry
            cancelAndIgnoreRemainingEvents()
        }
    }
}
