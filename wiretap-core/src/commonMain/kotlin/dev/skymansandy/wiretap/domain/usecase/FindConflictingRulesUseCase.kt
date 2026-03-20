package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository

class FindConflictingRulesUseCase internal constructor(
    private val ruleRepository: RuleRepository,
) {

    suspend operator fun invoke(rule: WiretapRule): List<WiretapRule> {
        return ruleRepository.getEnabledRules().filter { existing ->
            existing.id != rule.id && RuleMatcher.rulesOverlap(existing, rule)
        }
    }
}
