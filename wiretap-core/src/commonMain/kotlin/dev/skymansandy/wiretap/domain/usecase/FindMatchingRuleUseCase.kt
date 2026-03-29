/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository

class FindMatchingRuleUseCase internal constructor(
    private val ruleRepository: RuleRepository,
) {
    suspend operator fun invoke(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): WiretapRule? {
        return ruleRepository.getEnabledRules()
            .firstOrNull { rule ->
                val matchesMethod = RuleMatcher.matchesMethod(method, rule.method)
                val matchesCriteria = RuleMatcher.matchesAllCriteria(rule, url, headers, body)
                matchesMethod && matchesCriteria
            }
    }
}
