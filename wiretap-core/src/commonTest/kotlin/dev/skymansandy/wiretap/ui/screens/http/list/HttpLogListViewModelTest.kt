/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.list

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.model.StatusGroup
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HttpLogListViewModelTest {

    private val manager = mock<HttpLogManager>(MockMode.autoUnit)

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    @Test
    fun `availableHosts streams from the manager`() = runTest {
        every { manager.flowHttpLogs() } returns flowOf(emptyList())
        every { manager.flowDistinctHosts() } returns flowOf(listOf("a.com", "b.com"))
        every { manager.flowPagedHttpLogsForSearchQuery(any(), any()) } returns flowOf()

        val vm = HttpLogListViewModel(httpLogManager = manager)

        vm.availableHosts.test {
            awaitItem() shouldBe emptyList()
            awaitItem() shouldBe listOf("a.com", "b.com")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasLogs is true when manager emits non-empty list`() = runTest {
        val log = HttpLog(url = "x", method = "GET", timestamp = 0)
        every { manager.flowHttpLogs() } returns flowOf(listOf(log))
        every { manager.flowDistinctHosts() } returns flowOf(emptyList())
        every { manager.flowPagedHttpLogsForSearchQuery(any(), any()) } returns flowOf()

        val vm = HttpLogListViewModel(httpLogManager = manager)

        vm.hasLogs.test {
            awaitItem() shouldBe false
            awaitItem() shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applyFilter and clearFilters update the filter flow`() = runTest {
        every { manager.flowHttpLogs() } returns flowOf(emptyList())
        every { manager.flowDistinctHosts() } returns flowOf(emptyList())
        every { manager.flowPagedHttpLogsForSearchQuery(any(), any()) } returns flowOf()

        val vm = HttpLogListViewModel(httpLogManager = manager)
        vm.filter.value shouldBe HttpLogFilter()

        val populated = HttpLogFilter(statusGroups = setOf(StatusGroup.Success))
        vm.applyFilter(populated)
        vm.filter.value shouldBe populated

        vm.clearFilters()
        vm.filter.value shouldBe HttpLogFilter()
    }

    @Test
    fun `clearLogs delegates to the manager`() = runTest {
        every { manager.flowHttpLogs() } returns flowOf(emptyList())
        every { manager.flowDistinctHosts() } returns flowOf(emptyList())
        every { manager.flowPagedHttpLogsForSearchQuery(any(), any()) } returns flowOf()

        val vm = HttpLogListViewModel(httpLogManager = manager)
        vm.clearLogs()
        advanceUntilIdle()

        verifySuspend { manager.clearHttpLogs() }
    }
}
