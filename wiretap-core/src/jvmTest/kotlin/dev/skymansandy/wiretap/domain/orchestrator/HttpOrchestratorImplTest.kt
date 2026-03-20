package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.httpLogEntry
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class HttpOrchestratorImplTest {

    private lateinit var httpRepository: HttpRepository
    private lateinit var wiretapLogger: WiretapLogger
    private lateinit var orchestrator: HttpOrchestratorImpl

    @BeforeTest
    fun setup() {
        httpRepository = mock<HttpRepository>()
        wiretapLogger = mock<WiretapLogger>()
        orchestrator = HttpOrchestratorImpl(httpRepository, wiretapLogger)
    }

    @Test
    fun `logEntry saves to repository and logs`() = runTest {
        val entry = httpLogEntry()
        everySuspend { httpRepository.save(entry) } returns Unit
        every { wiretapLogger.logHttp(entry) } returns Unit

        orchestrator.logEntry(entry)

        verifySuspend { httpRepository.save(entry) }
        verify { wiretapLogger.logHttp(entry) }
    }

    @Test
    fun `logRequest saves and returns id`() = runTest {
        val entry = httpLogEntry()
        val expectedId = 42L
        everySuspend { httpRepository.saveAndGetId(entry) } returns expectedId
        every { wiretapLogger.logHttp(entry.copy(id = expectedId)) } returns Unit

        val id = orchestrator.logRequest(entry)

        id shouldBe expectedId
        verifySuspend { httpRepository.saveAndGetId(entry) }
        verify { wiretapLogger.logHttp(entry.copy(id = expectedId)) }
    }

    @Test
    fun `updateEntry updates repository and logs`() = runTest {
        val entry = httpLogEntry(id = 5)
        everySuspend { httpRepository.update(entry) } returns Unit
        every { wiretapLogger.logHttp(entry) } returns Unit

        orchestrator.updateEntry(entry)

        verifySuspend { httpRepository.update(entry) }
        verify { wiretapLogger.logHttp(entry) }
    }

    @Test
    fun `getAllLogs delegates to repository`() = runTest {
        val entries = listOf(httpLogEntry(id = 1), httpLogEntry(id = 2))
        every { httpRepository.getAll() } returns flowOf(entries)

        orchestrator.getAllLogs().test {
            awaitItem() shouldBe entries
            awaitComplete()
        }
    }

    @Test
    fun `getLogById delegates to repository`() = runTest {
        val entry = httpLogEntry(id = 1)
        everySuspend { httpRepository.getById(1L) } returns entry

        orchestrator.getLogById(1L) shouldBe entry
    }

    @Test
    fun `getLogById returns null when not found`() = runTest {
        everySuspend { httpRepository.getById(999L) } returns null

        orchestrator.getLogById(999L).shouldBeNull()
    }

    @Test
    fun `deleteLog delegates to repository`() = runTest {
        everySuspend { httpRepository.deleteById(1L) } returns Unit

        orchestrator.deleteLog(1L)

        verifySuspend { httpRepository.deleteById(1L) }
    }

    @Test
    fun `clearLogs clears repository`() = runTest {
        everySuspend { httpRepository.clearAll() } returns Unit

        orchestrator.clearLogs()

        verifySuspend { httpRepository.clearAll() }
    }

    @Test
    fun `purgeLogsOlderThan delegates to repository`() = runTest {
        val cutoff = 5000L
        everySuspend { httpRepository.deleteOlderThan(cutoff) } returns Unit

        orchestrator.purgeLogsOlderThan(cutoff)

        verifySuspend { httpRepository.deleteOlderThan(cutoff) }
    }
}
