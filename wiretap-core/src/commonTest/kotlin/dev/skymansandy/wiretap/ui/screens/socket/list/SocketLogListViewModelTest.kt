/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.socket.list

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SocketLogListViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val manager = mock<SocketLogManager>(MockMode.autoUnit)

    beforeEach { MainDispatcherSupport.setupMain() }
    afterEach { MainDispatcherSupport.teardownMain() }

    describe("updateSearchQuery") {
        it("does not crash and triggers downstream consumption") {
            runTest {
                every { manager.flowPagedSocketsForSearchQuery(any()) } returns flowOf()

                val vm = SocketLogListViewModel(socketLogManager = manager)
                vm.updateSearchQuery("hello")

                // Smoke: no exception thrown; managed flow allowed to settle.
                advanceUntilIdle()
            }
        }
    }

    describe("clearLogs") {
        it("delegates to the manager") {
            runTest {
                val vm = SocketLogListViewModel(socketLogManager = manager)

                vm.clearLogs()
                advanceUntilIdle()

                verifySuspend { manager.clearLogs() }
            }
        }
    }
})
