/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.first

internal class FindConflictingRulesUseCaseImpl(
    private val ruleRepository: RuleRepository,
) : FindConflictingRulesUseCase {
    override suspend fun invoke(other: WiretapRule): List<WiretapRule> {
        return ruleRepository.flowAll().first()
            .filter { existing ->
                existing.id != other.id && RuleMatcher.rulesOverlap(existing, other)
            }
    }
}
