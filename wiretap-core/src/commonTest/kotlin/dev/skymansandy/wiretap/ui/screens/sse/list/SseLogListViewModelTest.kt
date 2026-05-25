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
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SseLogListViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val manager = mock<SseLogManager>(MockMode.autoUnit)

    beforeEach { MainDispatcherSupport.setupMain() }
    afterEach { MainDispatcherSupport.teardownMain() }

    describe("updateSearchQuery") {
        it("is non-throwing") {
            runTest {
                every { manager.flowPagedConnectionsForSearchQuery(any()) } returns flowOf()

                val vm = SseLogListViewModel(sseLogManager = manager)
                vm.updateSearchQuery("query")
                advanceUntilIdle()
            }
        }
    }

    describe("clearLogs") {
        it("delegates to the manager") {
            runTest {
                val vm = SseLogListViewModel(sseLogManager = manager)

                vm.clearLogs()
                advanceUntilIdle()

                verifySuspend { manager.clearLogs() }
            }
        }
    }
})
