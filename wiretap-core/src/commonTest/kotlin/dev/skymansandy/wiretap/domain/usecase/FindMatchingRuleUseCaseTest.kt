package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.wiretapRule
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FindMatchingRuleUseCaseTest {

    private lateinit var ruleRepository: RuleRepository
    private lateinit var useCase: FindMatchingRuleUseCase

    @BeforeTest
    fun setup() {
        ruleRepository = mock<RuleRepository>()
        useCase = FindMatchingRuleUseCase(ruleRepository)
    }

    @Test
    fun `returns first matching rule by method and criteria`() = runTest {
        val rule = wiretapRule(
            id = 1,
            method = "GET",
            urlMatcher = UrlMatcher.Contains("api.example.com"),
        )
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule)

        val result = useCase(url = "https://api.example.com/users", method = "GET")

        result shouldBe rule
    }

    @Test
    fun `returns null when no rules match`() = runTest {
        val rule = wiretapRule(
            id = 1,
            method = "POST",
            urlMatcher = UrlMatcher.Contains("other.com"),
        )
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule)

        val result = useCase(url = "https://api.example.com/users", method = "GET")

        result.shouldBeNull()
    }

    @Test
    fun `returns null when no enabled rules exist`() = runTest {
        everySuspend { ruleRepository.getEnabledRules() } returns emptyList()

        val result = useCase(url = "https://api.example.com/users", method = "GET")

        result.shouldBeNull()
    }

    @Test
    fun `returns first match when multiple rules match`() = runTest {
        val rule1 = wiretapRule(
            id = 1,
            method = "*",
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 200),
        )
        val rule2 = wiretapRule(
            id = 2,
            method = "*",
            urlMatcher = UrlMatcher.Contains("api"),
            action = RuleAction.Mock(responseCode = 500),
        )
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule1, rule2)

        val result = useCase(url = "https://api.example.com/users", method = "GET")

        result shouldBe rule1
    }

    @Test
    fun `matches with headers and body`() = runTest {
        val rule = wiretapRule(
            id = 1,
            method = "POST",
            urlMatcher = UrlMatcher.Contains("api"),
            bodyMatcher = dev.skymansandy.wiretap.domain.model.BodyMatcher.Contains("user"),
        )
        everySuspend { ruleRepository.getEnabledRules() } returns listOf(rule)

        val result = useCase(
            url = "https://api.example.com",
            method = "POST",
            body = """{"user":"john"}""",
        )

        result shouldBe rule
    }
}
