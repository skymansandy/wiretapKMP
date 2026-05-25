/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class FindMatchingRuleUseCaseTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val repo = mock<RuleRepository>(MockMode.autoUnit)
    val useCase = FindMatchingRuleUseCase(repo)

    describe("invoke") {
        it("returns null when repository yields no enabled rules") {
            runTest {
                everySuspend { repo.getEnabledRules() } returns emptyList()

                useCase("https://example.com", "GET") shouldBe null
            }
        }

        it("returns the first rule whose method and criteria both match") {
            runTest {
                val first = rule(id = 1, method = "POST", url = UrlMatcher.Contains("/users"))
                val second = rule(id = 2, method = "GET", url = UrlMatcher.Contains("/users"))
                val third = rule(id = 3, method = "*", url = UrlMatcher.Contains("/users"))
                everySuspend { repo.getEnabledRules() } returns listOf(first, second, third)

                val matched = useCase(url = "https://example.com/users", method = "GET")

                matched?.id shouldBe 2
            }
        }

        it("skips rules whose method does not match the request") {
            runTest {
                val onlyPost = rule(id = 1, method = "POST", url = UrlMatcher.Exact("https://x"))
                everySuspend { repo.getEnabledRules() } returns listOf(onlyPost)

                useCase("https://x", "GET") shouldBe null
            }
        }

        it("wildcard method rule matches any request method") {
            runTest {
                val wildcard = rule(id = 1, method = "*", url = UrlMatcher.Contains("/api"))
                everySuspend { repo.getEnabledRules() } returns listOf(wildcard)

                useCase("https://example.com/api", "DELETE")?.id shouldBe 1
            }
        }

        it("rule with no criteria never matches") {
            runTest {
                val empty = rule(id = 1, method = "*", url = null)
                everySuspend { repo.getEnabledRules() } returns listOf(empty)

                useCase("https://example.com", "GET") shouldBe null
            }
        }

        it("header matchers are evaluated against the request headers") {
            runTest {
                val needsAuth = rule(
                    id = 1,
                    method = "*",
                    url = UrlMatcher.Contains("/secure"),
                    headers = listOf(HeaderMatcher.KeyExists("Authorization")),
                )
                everySuspend { repo.getEnabledRules() } returns listOf(needsAuth)

                useCase("https://example.com/secure", "GET", emptyMap()) shouldBe null
                useCase(
                    "https://example.com/secure",
                    "GET",
                    mapOf("Authorization" to "Bearer x"),
                )?.id shouldBe 1
            }
        }
    }
})

private fun rule(
    id: Long,
    method: String,
    url: UrlMatcher?,
    headers: List<HeaderMatcher> = emptyList(),
) = WiretapRule(
    id = id,
    method = method,
    urlMatcher = url,
    headerMatchers = headers,
    action = RuleAction.Mock(),
)
