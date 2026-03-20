package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.wiretapRule
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FindConflictingRulesUseCaseTest {

    private lateinit var ruleRepository: RuleRepository
    private lateinit var useCase: FindConflictingRulesUseCase

    @BeforeTest
    fun setup() {
        ruleRepository = mock<RuleRepository>()
        useCase = FindConflictingRulesUseCase(ruleRepository)
    }

    @Test
    fun `returns empty list when no conflicting rules exist`() = runTest {
        val rule = wiretapRule(id = 1, method = "GET", urlMatcher = UrlMatcher.Exact("https://a.com"))
        val other = wiretapRule(id = 2, method = "POST", urlMatcher = UrlMatcher.Exact("https://b.com"))
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule, other)

        val result = useCase(rule)

        result.shouldBeEmpty()
    }

    @Test
    fun `returns conflicting rules that overlap`() = runTest {
        val rule = wiretapRule(id = 1, method = "GET", urlMatcher = UrlMatcher.Contains("api"))
        val conflicting = wiretapRule(id = 2, method = "GET", urlMatcher = UrlMatcher.Contains("api"))
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule, conflicting)

        val result = useCase(rule)

        result.shouldContainExactly(conflicting)
    }

    @Test
    fun `excludes the rule itself from conflicts`() = runTest {
        val rule = wiretapRule(id = 1, method = "*", urlMatcher = UrlMatcher.Contains("api"))
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule)

        val result = useCase(rule)

        result.shouldBeEmpty()
    }

    @Test
    fun `returns multiple conflicting rules`() = runTest {
        val rule = wiretapRule(id = 1, method = "*", urlMatcher = UrlMatcher.Contains("api"))
        val conflict1 = wiretapRule(id = 2, method = "GET", urlMatcher = UrlMatcher.Contains("api"))
        val conflict2 = wiretapRule(id = 3, method = "POST", urlMatcher = UrlMatcher.Contains("api"))
        val noConflict = wiretapRule(id = 4, method = "GET", urlMatcher = UrlMatcher.Exact("https://other.com"))
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule, conflict1, conflict2, noConflict)

        val result = useCase(rule)

        result shouldHaveSize 2
        result.shouldContainExactly(conflict1, conflict2)
    }
}
