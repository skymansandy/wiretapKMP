package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.verify
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.httpLogEntry
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpOrchestratorImplTest {

    private lateinit var networkRepository: NetworkRepository
    private lateinit var networkLogger: NetworkLogger
    private lateinit var orchestrator: HttpOrchestratorImpl

    @BeforeTest
    fun setup() {
        networkRepository = mock<NetworkRepository>()
        networkLogger = mock<NetworkLogger>()
        orchestrator = HttpOrchestratorImpl(networkRepository, networkLogger)
    }

    @Test
    fun `logEntry saves to repository and logs`() = runTest {
        val entry = httpLogEntry()
        everySuspend { networkRepository.save(entry) } returns Unit
        every { networkLogger.logHttp(entry) } returns Unit

        orchestrator.logEntry(entry)

        verifySuspend { networkRepository.save(entry) }
        verify { networkLogger.logHttp(entry) }
    }

    @Test
    fun `logRequest saves and returns id`() = runTest {
        val entry = httpLogEntry()
        val expectedId = 42L
        everySuspend { networkRepository.saveAndGetId(entry) } returns expectedId
        every { networkLogger.logHttp(entry.copy(id = expectedId)) } returns Unit

        val id = orchestrator.logRequest(entry)

        id shouldBe expectedId
        verifySuspend { networkRepository.saveAndGetId(entry) }
        verify { networkLogger.logHttp(entry.copy(id = expectedId)) }
    }

    @Test
    fun `updateEntry updates repository and logs`() = runTest {
        val entry = httpLogEntry(id = 5)
        everySuspend { networkRepository.update(entry) } returns Unit
        every { networkLogger.logHttp(entry) } returns Unit

        orchestrator.updateEntry(entry)

        verifySuspend { networkRepository.update(entry) }
        verify { networkLogger.logHttp(entry) }
    }

    @Test
    fun `getAllLogs delegates to repository`() = runTest {
        val entries = listOf(httpLogEntry(id = 1), httpLogEntry(id = 2))
        every { networkRepository.getAll() } returns flowOf(entries)

        orchestrator.getAllLogs().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `getLogById delegates to repository`() = runTest {
        val entry = httpLogEntry(id = 1)
        everySuspend { networkRepository.getById(1L) } returns entry

        orchestrator.getLogById(1L) shouldBe entry
    }

    @Test
    fun `getLogById returns null when not found`() = runTest {
        everySuspend { networkRepository.getById(999L) } returns null

        orchestrator.getLogById(999L).shouldBeNull()
    }

    @Test
    fun `deleteLog delegates to repository`() = runTest {
        everySuspend { networkRepository.deleteById(1L) } returns Unit

        orchestrator.deleteLog(1L)

        verifySuspend { networkRepository.deleteById(1L) }
    }

    @Test
    fun `clearLogs clears repository`() = runTest {
        everySuspend { networkRepository.clearAll() } returns Unit

        orchestrator.clearLogs()

        verifySuspend { networkRepository.clearAll() }
    }

    @Test
    fun `purgeLogsOlderThan delegates to repository`() = runTest {
        val cutoff = 5000L
        everySuspend { networkRepository.deleteOlderThan(cutoff) } returns Unit

        orchestrator.purgeLogsOlderThan(cutoff)

        verifySuspend { networkRepository.deleteOlderThan(cutoff) }
    }
}
