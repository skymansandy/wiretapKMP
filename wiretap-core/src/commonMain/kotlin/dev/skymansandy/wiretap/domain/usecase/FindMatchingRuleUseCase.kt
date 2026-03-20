package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository

class FindMatchingRuleUseCase constructor(
    private val ruleRepository: RuleRepository,
) {
    suspend operator fun invoke(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): WiretapRule? {
        return ruleRepository.getEnabledRules().firstOrNull { rule ->
            RuleMatcher.matchesMethod(method, rule.method) &&
                RuleMatcher.matchesAllCriteria(rule, url, headers, body)
        }
    }
}
