/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.view

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
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
class RuleDetailViewModelTest {

    private val repo = mock<RuleRepository>(MockMode.autoUnit)

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    @Test
    fun `rule and enabled track the repository flow`() = runTest {
        val rule = WiretapRule(id = 5, method = "GET", action = RuleAction.Mock(), enabled = true)
        every { repo.flowById(5) } returns flowOf(rule)

        val vm = RuleDetailViewModel(ruleId = 5, ruleRepository = repo)

        vm.rule.test {
            awaitItem() shouldBe null
            awaitItem() shouldBe rule
            cancelAndIgnoreRemainingEvents()
        }
        vm.enabled.test {
            awaitItem() shouldBe false
            awaitItem() shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleEnabled delegates to repository`() = runTest {
        every { repo.flowById(5) } returns flowOf(null)

        val vm = RuleDetailViewModel(ruleId = 5, ruleRepository = repo)
        vm.toggleEnabled(true)
        advanceUntilIdle()

        verifySuspend { repo.setEnabled(5, true) }
    }

    @Test
    fun `requestDelete and dismissDelete toggle the confirmation flag`() = runTest {
        every { repo.flowById(5) } returns flowOf(null)

        val vm = RuleDetailViewModel(ruleId = 5, ruleRepository = repo)

        vm.showDeleteConfirm.value shouldBe false
        vm.requestDelete()
        vm.showDeleteConfirm.value shouldBe true
        vm.dismissDelete()
        vm.showDeleteConfirm.value shouldBe false
    }

    @Test
    fun `confirmDelete invokes repository deleteById and then onDeleted callback`() = runTest {
        every { repo.flowById(5) } returns flowOf(null)
        var deleted = false

        val vm = RuleDetailViewModel(ruleId = 5, ruleRepository = repo)
        vm.confirmDelete { deleted = true }
        advanceUntilIdle()

        verifySuspend { repo.deleteById(5) }
        deleted shouldBe true
    }
}
