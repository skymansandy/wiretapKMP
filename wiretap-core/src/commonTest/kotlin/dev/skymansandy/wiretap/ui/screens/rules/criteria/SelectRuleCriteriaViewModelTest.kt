/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.criteria

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SelectRuleCriteriaViewModelTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val manager = mock<HttpLogManager>(MockMode.autoUnit)
    val io = UnconfinedTestDispatcher()

    beforeEach { MainDispatcherSupport.setupMain() }
    afterEach { MainDispatcherSupport.teardownMain() }

    fun newVm(logId: Long) = SelectRuleCriteriaViewModel(
        logId = logId,
        httpLogManager = manager,
        ioDispatcher = io,
    )

    describe("state") {
        it("stays initial when manager returns null") {
            runTest {
                everySuspend { manager.getHttpLogById(99) } returns null

                val vm = newVm(logId = 99)
                advanceUntilIdle()

                vm.state.value shouldBe SelectRuleCriteriaState()
            }
        }

        it("seeds includeBody true when log has a non-empty request body") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "payload")

                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.state.value.includeBody shouldBe true
                vm.state.value.includeUrl shouldBe true
                vm.state.value.selectedHeaderKeys shouldBe emptySet()
            }
        }

        it("seeds includeBody false when log has an empty request body") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "")

                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.state.value.includeBody shouldBe false
            }
        }
    }

    describe("toggleUrl") {
        it("flips includeUrl") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log()
                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.toggleUrl()
                vm.state.value.includeUrl shouldBe false
                vm.toggleUrl()
                vm.state.value.includeUrl shouldBe true
            }
        }
    }

    describe("toggleBody") {
        it("flips includeBody") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "payload")
                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.state.value.includeBody shouldBe true
                vm.toggleBody()
                vm.state.value.includeBody shouldBe false
            }
        }
    }

    describe("toggleAllHeaders") {
        it("selects every header on first call and clears on second") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log(
                    requestHeaders = mapOf("Authorization" to "abc", "Accept" to "json"),
                )
                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.state.value.selectedHeaderKeys shouldBe emptySet()
                vm.toggleAllHeaders()
                vm.state.value.selectedHeaderKeys shouldBe setOf("Authorization", "Accept")
                vm.toggleAllHeaders()
                vm.state.value.selectedHeaderKeys shouldBe emptySet()
            }
        }
    }

    describe("toggleHeaderKey") {
        it("adds and removes the key") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log()
                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.toggleHeaderKey("Authorization")
                vm.state.value.selectedHeaderKeys shouldBe setOf("Authorization")
                vm.toggleHeaderKey("Authorization")
                vm.state.value.selectedHeaderKeys shouldBe emptySet()
            }
        }
    }

    describe("includeHeaders") {
        it("is true exactly when selectedHeaderKeys is non-empty") {
            runTest {
                everySuspend { manager.getHttpLogById(1) } returns log()
                val vm = newVm(logId = 1)
                advanceUntilIdle()

                vm.state.value.includeHeaders shouldBe false
                vm.toggleHeaderKey("Authorization")
                vm.state.value.includeHeaders shouldBe true
            }
        }
    }

    describe("allHeadersSelected") {
        it("is false when log is null or has no headers") {
            SelectRuleCriteriaState(httpLog = null).allHeadersSelected shouldBe false
            SelectRuleCriteriaState(httpLog = log()).allHeadersSelected shouldBe false
        }

        it("reflects whether selectedHeaderKeys covers every header") {
            val state = SelectRuleCriteriaState(
                httpLog = log(requestHeaders = mapOf("a" to "1", "b" to "2")),
                selectedHeaderKeys = setOf("a"),
            )
            state.allHeadersSelected shouldBe false

            state.copy(selectedHeaderKeys = setOf("a", "b")).allHeadersSelected shouldBe true
        }
    }
})

private fun log(
    requestHeaders: Map<String, String> = emptyMap(),
    requestBody: String? = null,
) = HttpLog(
    id = 1,
    url = "https://x",
    method = "GET",
    requestHeaders = requestHeaders,
    requestBody = requestBody,
    timestamp = 0,
)
