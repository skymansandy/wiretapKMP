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
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectRuleCriteriaViewModelTest {

    private val manager = mock<HttpLogManager>(MockMode.autoUnit)
    private val io = UnconfinedTestDispatcher()

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    private fun newVm(logId: Long) = SelectRuleCriteriaViewModel(
        logId = logId,
        httpLogManager = manager,
        ioDispatcher = io,
    )

    @Test
    fun `state stays initial when manager returns null`() = runTest {
        everySuspend { manager.getHttpLogById(99) } returns null

        val vm = newVm(logId = 99)
        advanceUntilIdle()

        vm.state.value shouldBe SelectRuleCriteriaState()
    }

    @Test
    fun `state seeds includeBody true when log has a non-empty request body`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "payload")

        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.state.value.includeBody shouldBe true
        vm.state.value.includeUrl shouldBe true
        vm.state.value.selectedHeaderKeys shouldBe emptySet()
    }

    @Test
    fun `state seeds includeBody false when log has an empty request body`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "")

        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.state.value.includeBody shouldBe false
    }

    @Test
    fun `toggleUrl flips includeUrl`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log()
        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.toggleUrl()
        vm.state.value.includeUrl shouldBe false
        vm.toggleUrl()
        vm.state.value.includeUrl shouldBe true
    }

    @Test
    fun `toggleBody flips includeBody`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log(requestBody = "payload")
        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.state.value.includeBody shouldBe true
        vm.toggleBody()
        vm.state.value.includeBody shouldBe false
    }

    @Test
    fun `toggleAllHeaders selects every header on first call and clears on second`() = runTest {
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

    @Test
    fun `toggleHeaderKey adds and removes the key`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log()
        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.toggleHeaderKey("Authorization")
        vm.state.value.selectedHeaderKeys shouldBe setOf("Authorization")
        vm.toggleHeaderKey("Authorization")
        vm.state.value.selectedHeaderKeys shouldBe emptySet()
    }

    @Test
    fun `includeHeaders is true exactly when selectedHeaderKeys is non-empty`() = runTest {
        everySuspend { manager.getHttpLogById(1) } returns log()
        val vm = newVm(logId = 1)
        advanceUntilIdle()

        vm.state.value.includeHeaders shouldBe false
        vm.toggleHeaderKey("Authorization")
        vm.state.value.includeHeaders shouldBe true
    }

    @Test
    fun `allHeadersSelected is false when log is null or has no headers`() {
        SelectRuleCriteriaState(httpLog = null).allHeadersSelected shouldBe false
        SelectRuleCriteriaState(httpLog = log()).allHeadersSelected shouldBe false
    }

    @Test
    fun `allHeadersSelected reflects whether selectedHeaderKeys covers every header`() {
        val state = SelectRuleCriteriaState(
            httpLog = log(requestHeaders = mapOf("a" to "1", "b" to "2")),
            selectedHeaderKeys = setOf("a"),
        )
        state.allHeadersSelected shouldBe false

        state.copy(selectedHeaderKeys = setOf("a", "b")).allHeadersSelected shouldBe true
    }

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
}
