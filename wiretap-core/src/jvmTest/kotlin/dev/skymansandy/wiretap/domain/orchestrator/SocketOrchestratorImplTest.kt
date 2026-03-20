package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.verify
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.socketLogEntry
import dev.skymansandy.wiretap.socketMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SocketOrchestratorImplTest {

    private lateinit var socketRepository: SocketRepository
    private lateinit var networkLogger: NetworkLogger
    private lateinit var orchestrator: SocketOrchestratorImpl

    @BeforeTest
    fun setup() {
        socketRepository = mock<SocketRepository>()
        networkLogger = mock<NetworkLogger>()
        orchestrator = SocketOrchestratorImpl(socketRepository, networkLogger)
    }

    @Test
    fun `openSocketConnection saves and returns id`() = runTest {
        val entry = socketLogEntry()
        val expectedId = 10L
        everySuspend { socketRepository.openConnection(entry) } returns expectedId
        every { networkLogger.logSocket(entry.copy(id = expectedId)) } returns Unit

        val id = orchestrator.openSocketConnection(entry)

        id shouldBe expectedId
        verifySuspend { socketRepository.openConnection(entry) }
    }

    @Test
    fun `updateSocketConnection updates repository and removes from cache when closed`() = runTest {
        val entry = socketLogEntry(id = 10, status = SocketStatus.Closed)
        everySuspend { socketRepository.updateConnection(entry) } returns Unit
        every { networkLogger.logSocket(entry) } returns Unit

        orchestrator.updateSocketConnection(entry)

        verifySuspend { socketRepository.updateConnection(entry) }
        verify { networkLogger.logSocket(entry) }
    }

    @Test
    fun `updateSocketConnection keeps cache when status is open`() = runTest {
        // First open a connection to populate cache
        val entry = socketLogEntry(id = 10, status = SocketStatus.Open)
        everySuspend { socketRepository.openConnection(entry) } returns 10L
        every { networkLogger.logSocket(entry.copy(id = 10)) } returns Unit
        orchestrator.openSocketConnection(entry)

        // Then update with Open status
        val updatedEntry = entry.copy(id = 10, status = SocketStatus.Open)
        everySuspend { socketRepository.updateConnection(updatedEntry) } returns Unit
        every { networkLogger.logSocket(updatedEntry) } returns Unit

        orchestrator.updateSocketConnection(updatedEntry)

        verifySuspend { socketRepository.updateConnection(updatedEntry) }
    }

    @Test
    fun `logSocketMessage logs message and notifies`() = runTest {
        val message = socketMessage(socketId = 10)
        val entry = socketLogEntry(id = 10)
        everySuspend { socketRepository.getById(10L) } returns entry
        everySuspend { socketRepository.logMessage(message) } returns Unit
        every { networkLogger.logSocketMessage(message) } returns Unit

        orchestrator.logSocketMessage(message)

        verifySuspend { socketRepository.logMessage(message) }
        verify { networkLogger.logSocketMessage(message) }
    }

    @Test
    fun `logSocketMessage reopens connection when entry cleared but cached`() = runTest {
        // Open connection first to populate cache
        val entry = socketLogEntry(status = SocketStatus.Open)
        everySuspend { socketRepository.openConnection(entry) } returns 10L
        every { networkLogger.logSocket(entry.copy(id = 10)) } returns Unit
        orchestrator.openSocketConnection(entry)

        // Simulate cleared DB - getById returns null first, then returns after reopen
        val message = socketMessage(socketId = 10)
        val reopenedEntry = entry.copy(id = 10, historyCleared = true, messageCount = 0)
        everySuspend { socketRepository.getById(10L) } returns null
        everySuspend { socketRepository.reopenConnection(reopenedEntry) } returns Unit
        everySuspend { socketRepository.logMessage(message) } returns Unit
        every { networkLogger.logSocketMessage(message) } returns Unit

        orchestrator.logSocketMessage(message)

        verifySuspend { socketRepository.reopenConnection(reopenedEntry) }
        verifySuspend { socketRepository.logMessage(message) }
    }

    @Test
    fun `getSocketById delegates to repository`() = runTest {
        val entry = socketLogEntry(id = 5)
        everySuspend { socketRepository.getById(5L) } returns entry

        orchestrator.getSocketById(5L) shouldBe entry
    }

    @Test
    fun `getSocketById returns null when not found`() = runTest {
        everySuspend { socketRepository.getById(999L) } returns null

        orchestrator.getSocketById(999L).shouldBeNull()
    }

    @Test
    fun `getSocketByIdFlow delegates to repository`() = runTest {
        val entry = socketLogEntry(id = 5)
        every { socketRepository.getByIdFlow(5L) } returns flowOf(entry)

        orchestrator.getSocketByIdFlow(5L).test {
            awaitItem() shouldBe entry
            awaitComplete()
        }
    }

    @Test
    fun `getSocketMessages delegates to repository`() = runTest {
        val messages = listOf(socketMessage(id = 1), socketMessage(id = 2))
        every { socketRepository.getMessages(5L) } returns flowOf(messages)

        orchestrator.getSocketMessages(5L).test {
            awaitItem() shouldBe messages
            awaitComplete()
        }
    }

    @Test
    fun `getAllSocketLogs delegates to repository`() = runTest {
        val entries = listOf(socketLogEntry(id = 1), socketLogEntry(id = 2))
        every { socketRepository.getAll() } returns flowOf(entries)

        orchestrator.getAllSocketLogs().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `clearSocketLogs clears repository`() = runTest {
        everySuspend { socketRepository.clearAll() } returns Unit

        orchestrator.clearSocketLogs()

        verifySuspend { socketRepository.clearAll() }
    }
}
