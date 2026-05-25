/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.list

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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RulesListViewModelTest {

    private val repo = mock<RuleRepository>(MockMode.autoUnit)

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    @Test
    fun `empty query uses flowAll and non-blank query routes to flowForQuery after debounce`() =
        runTest {
            val allRules = listOf(rule(1), rule(2))
            val searchRules = listOf(rule(3))
            every { repo.flowAll() } returns flowOf(allRules)
            every { repo.flowForQuery("token") } returns flowOf(searchRules)

            val vm = RulesListViewModel(ruleRepository = repo)

            vm.rules.test {
                awaitItem() shouldBe emptyList()
                // The debounced empty query fires immediately, then flowAll emits.
                awaitItem() shouldBe allRules

                vm.updateSearchQuery("token")
                advanceTimeBy(500) // exceed 450ms debounce
                awaitItem() shouldBe searchRules

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setEnabled delegates to the repository`() = runTest {
        every { repo.flowAll() } returns flowOf(emptyList())

        val vm = RulesListViewModel(ruleRepository = repo)
        vm.setEnabled(id = 5, enabled = false)
        advanceUntilIdle()

        verifySuspend { repo.setEnabled(5, false) }
    }

    private fun rule(id: Long) = WiretapRule(
        id = id,
        method = "GET",
        action = RuleAction.Mock(),
    )
}
