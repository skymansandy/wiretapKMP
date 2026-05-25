/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.WiretapRule

interface FindConflictingRulesUseCase {
    suspend operator fun invoke(other: WiretapRule): List<WiretapRule>
}
