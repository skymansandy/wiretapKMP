/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.WiretapRule

interface FindMatchingRuleUseCase {
    suspend operator fun invoke(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): WiretapRule?
}
