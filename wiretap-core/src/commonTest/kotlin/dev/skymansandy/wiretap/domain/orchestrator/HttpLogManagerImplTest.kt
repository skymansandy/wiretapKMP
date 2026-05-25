/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class HttpLogManagerImplTest {

    private val repository = mock<HttpRepository>(MockMode.autoUnit)
    private val logger = mock<WiretapLogger>(MockMode.autoUnit)
    private val hook = mock<WiretapNotificationHook>(MockMode.autoUnit)
    private val manager = HttpLogManagerImpl(repository, logger, hook)

    @Test
    fun `flowHttpLogs delegates to repository`() = runTest {
        every { repository.flowAll() } returns flowOf(emptyList())

        manager.flowHttpLogs()

        verify { repository.flowAll() }
    }

    @Test
    fun `flowDistinctHosts delegates to repository`() = runTest {
        every { repository.flowDistinctHosts() } returns flowOf(emptyList())

        manager.flowDistinctHosts()

        verify { repository.flowDistinctHosts() }
    }

    @Test
    fun `flowHttpLogById delegates to repository`() = runTest {
        every { repository.flowById(7) } returns flowOf(null)

        manager.flowHttpLogById(7)

        verify { repository.flowById(7) }
    }

    @Test
    fun `flowPagedHttpLogsForSearchQuery passes query and filter through`() = runTest {
        val filter = HttpLogFilter()
        every { repository.flowPagesLogs("q", filter) } returns flowOf()

        manager.flowPagedHttpLogsForSearchQuery("q", filter)

        verify { repository.flowPagesLogs("q", filter) }
    }

    @Test
    fun `logHttp calls repository save, logger and hook in order`() = runTest {
        val entry = httpLog(url = "https://x")

        manager.logHttp(entry)

        verifySuspend { repository.save(entry) }
        verify { logger.logHttp(entry) }
        verify { hook.onNewHttpLog(entry) }
    }

    @Test
    fun `logHttpAndGetId returns the id and forwards a copy with id to logger and hook`() = runTest {
        val original = httpLog(url = "https://x")
        everySuspend { repository.saveAndGetId(original) } returns 99L

        val id = manager.logHttpAndGetId(original)

        id shouldBe 99L
        verify { logger.logHttp(matches<HttpLog> { it.id == 99L && it.url == "https://x" }) }
        verify { hook.onNewHttpLog(matches<HttpLog> { it.id == 99L && it.url == "https://x" }) }
    }

    @Test
    fun `updateHttp delegates to repository, logger and hook`() = runTest {
        val entry = httpLog(url = "https://x")

        manager.updateHttp(entry)

        verifySuspend { repository.update(entry) }
        verify { logger.logHttp(entry) }
        verify { hook.onNewHttpLog(entry) }
    }

    @Test
    fun `getHttpLogById delegates to repository`() = runTest {
        everySuspend { repository.getById(7) } returns httpLog().copy(id = 7)

        manager.getHttpLogById(7)?.id shouldBe 7L
    }

    @Test
    fun `deleteHttpLog notifies hook with the deleted id`() = runTest {
        manager.deleteHttpLog(11)

        verifySuspend { repository.deleteById(11) }
        verify { hook.onDeleteHttpLog(11) }
    }

    @Test
    fun `clearHttpLogs clears the repository and notifies the hook`() = runTest {
        manager.clearHttpLogs()

        verifySuspend { repository.clearAll() }
        verify { hook.onClearHttpLogs() }
    }

    @Test
    fun `purgeHttpLogsOlderThan delegates timestamp to repository`() = runTest {
        manager.purgeHttpLogsOlderThan(cutoffMs = 100L)

        verifySuspend { repository.deleteOlderThan(100L) }
    }

    @Test
    fun `markHttpCancelledIfInProgress delegates to repository`() = runTest {
        manager.markHttpCancelledIfInProgress(5)

        verifySuspend { repository.markCancelledIfInProgress(5) }
    }

    private fun httpLog(url: String = "https://x") = HttpLog(
        url = url,
        method = "GET",
        timestamp = 0,
    )
}
