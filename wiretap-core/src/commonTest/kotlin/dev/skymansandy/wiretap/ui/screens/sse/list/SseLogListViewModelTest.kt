/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.list

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SseLogListViewModelTest {

    private val manager = mock<SseLogManager>(MockMode.autoUnit)

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    @Test
    fun `updateSearchQuery is non-throwing`() = runTest {
        every { manager.flowPagedConnectionsForSearchQuery(any()) } returns flowOf()

        val vm = SseLogListViewModel(sseLogManager = manager)
        vm.updateSearchQuery("query")
        advanceUntilIdle()
    }

    @Test
    fun `clearLogs delegates to the manager`() = runTest {
        val vm = SseLogListViewModel(sseLogManager = manager)

        vm.clearLogs()
        advanceUntilIdle()

        verifySuspend { manager.clearLogs() }
    }
}
