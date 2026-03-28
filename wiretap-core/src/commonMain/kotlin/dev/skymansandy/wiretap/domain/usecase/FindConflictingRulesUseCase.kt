package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.first

class FindConflictingRulesUseCase internal constructor(
    private val ruleRepository: RuleRepository,
) {

    suspend operator fun invoke(other: WiretapRule): List<WiretapRule> {
        return ruleRepository.flowAll().first()
            .filter { existing ->
                existing.id != other.id && RuleMatcher.rulesOverlap(existing, other)
            }
    }
}
