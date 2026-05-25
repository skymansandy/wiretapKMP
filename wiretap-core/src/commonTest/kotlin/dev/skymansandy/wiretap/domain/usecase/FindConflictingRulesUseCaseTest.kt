/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class FindConflictingRulesUseCaseTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val repo = mock<RuleRepository>(MockMode.autoUnit)
    val useCase = FindConflictingRulesUseCase(repo)

    describe("invoke") {
        it("returns empty list when no other rules exist") {
            runTest {
                every { repo.flowAll() } returns flowOf(emptyList())

                useCase(rule(id = 1, url = UrlMatcher.Exact("/a"))) shouldBe emptyList()
            }
        }

        it("excludes the rule being checked from conflicts") {
            runTest {
                val candidate = rule(id = 1, url = UrlMatcher.Exact("/api/users"))
                every { repo.flowAll() } returns flowOf(listOf(candidate))

                useCase(candidate) shouldBe emptyList()
            }
        }

        it("returns every other rule whose criteria overlap") {
            runTest {
                val candidate = rule(id = 10, url = UrlMatcher.Exact("/api/users"))
                val conflictingExact = rule(id = 1, url = UrlMatcher.Exact("/api/users"))
                val conflictingContains = rule(id = 2, url = UrlMatcher.Contains("users"))
                val nonConflict = rule(id = 3, url = UrlMatcher.Exact("/api/admins"))

                every { repo.flowAll() } returns flowOf(
                    listOf(conflictingExact, conflictingContains, nonConflict, candidate),
                )

                useCase(candidate).map { it.id } shouldContainExactlyInAnyOrder listOf(1L, 2L)
            }
        }

        it("considers disabled rules when computing conflicts") {
            runTest {
                val candidate = rule(id = 10, url = UrlMatcher.Exact("/x"))
                val disabledOverlap = rule(id = 1, url = UrlMatcher.Exact("/x"), enabled = false)

                every { repo.flowAll() } returns flowOf(listOf(disabledOverlap, candidate))

                useCase(candidate).map { it.id } shouldBe listOf(1L)
            }
        }
    }
})

private fun rule(
    id: Long,
    url: UrlMatcher?,
    method: String = "*",
    enabled: Boolean = true,
) = WiretapRule(
    id = id,
    method = method,
    urlMatcher = url,
    action = RuleAction.Mock(),
    enabled = enabled,
)
