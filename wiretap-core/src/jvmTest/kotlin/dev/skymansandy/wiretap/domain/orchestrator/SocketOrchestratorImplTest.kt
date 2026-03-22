package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.socketLogEntry
import dev.skymansandy.wiretap.socketMessage
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SocketOrchestratorImplTest {

    private lateinit var socketRepository: SocketRepository
    private lateinit var wiretapLogger: WiretapLogger
    private lateinit var orchestrator: SocketOrchestratorImpl

    @BeforeTest
    fun setup() {
        socketRepository = mock<SocketRepository>()
        wiretapLogger = mock<WiretapLogger>()
        orchestrator = SocketOrchestratorImpl(socketRepository, wiretapLogger)
    }

    @Test
    fun `openSocketConnection saves and returns id`() = runTest {
        val entry = socketLogEntry()
        val expectedId = 10L
        everySuspend { socketRepository.openConnection(entry) } returns expectedId
        every { wiretapLogger.logSocket(entry.copy(id = expectedId)) } returns Unit

        val id = orchestrator.openSocket(entry)

        id shouldBe expectedId
        verifySuspend { socketRepository.openConnection(entry) }
    }

    @Test
    fun `updateSocketConnection updates repository and removes from cache when closed`() = runTest {
        val entry = socketLogEntry(id = 10, status = SocketStatus.Closed)
        everySuspend { socketRepository.updateConnection(entry) } returns Unit
        every { wiretapLogger.logSocket(entry) } returns Unit

        orchestrator.updateSocket(entry)

        verifySuspend { socketRepository.updateConnection(entry) }
        verify { wiretapLogger.logSocket(entry) }
    }

    @Test
    fun `updateSocketConnection keeps cache when status is open`() = runTest {
        // First open a connection to populate cache
        val entry = socketLogEntry(id = 10, status = SocketStatus.Open)
        everySuspend { socketRepository.openConnection(entry) } returns 10L
        every { wiretapLogger.logSocket(entry.copy(id = 10)) } returns Unit
        orchestrator.openSocket(entry)

        // Then update with Open status
        val updatedEntry = entry.copy(id = 10, status = SocketStatus.Open)
        everySuspend { socketRepository.updateConnection(updatedEntry) } returns Unit
        every { wiretapLogger.logSocket(updatedEntry) } returns Unit

        orchestrator.updateSocket(updatedEntry)

        verifySuspend { socketRepository.updateConnection(updatedEntry) }
    }

    @Test
    fun `logSocketMessage logs message and notifies`() = runTest {
        val message = socketMessage(socketId = 10)
        val entry = socketLogEntry(id = 10)
        everySuspend { socketRepository.getById(10L) } returns entry
        everySuspend { socketRepository.logMessage(message) } returns Unit
        every { wiretapLogger.logSocketMessage(message) } returns Unit

        orchestrator.logSocketMsg(message)

        verifySuspend { socketRepository.logMessage(message) }
        verify { wiretapLogger.logSocketMessage(message) }
    }

    @Test
    fun `logSocketMessage reopens connection when entry cleared but cached`() = runTest {
        // Open connection first to populate cache
        val entry = socketLogEntry(status = SocketStatus.Open)
        everySuspend { socketRepository.openConnection(entry) } returns 10L
        every { wiretapLogger.logSocket(entry.copy(id = 10)) } returns Unit
        orchestrator.openSocket(entry)

        // Simulate cleared DB - getById returns null first, then returns after reopen
        val message = socketMessage(socketId = 10)
        val reopenedEntry = entry.copy(id = 10, historyCleared = true, messageCount = 0)
        everySuspend { socketRepository.getById(10L) } returns null
        everySuspend { socketRepository.reopenConnection(reopenedEntry) } returns Unit
        everySuspend { socketRepository.logMessage(message) } returns Unit
        every { wiretapLogger.logSocketMessage(message) } returns Unit

        orchestrator.logSocketMsg(message)

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

        orchestrator.flowSocketById(5L).test {
            awaitItem() shouldBe entry
            awaitComplete()
        }
    }

    @Test
    fun `getSocketMessages delegates to repository`() = runTest {
        val messages = listOf(socketMessage(id = 1), socketMessage(id = 2))
        every { socketRepository.getMessages(5L) } returns flowOf(messages)

        orchestrator.flowSocketMessagesById(5L).test {
            awaitItem() shouldBe messages
            awaitComplete()
        }
    }

    @Test
    fun `getAllSocketLogs delegates to repository`() = runTest {
        val entries = listOf(socketLogEntry(id = 1), socketLogEntry(id = 2))
        every { socketRepository.getAll() } returns flowOf(entries)

        orchestrator.getAllSockets().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `clearSocketLogs clears repository`() = runTest {
        everySuspend { socketRepository.clearAll() } returns Unit

        orchestrator.clearLogs()

        verifySuspend { socketRepository.clearAll() }
    }
}
