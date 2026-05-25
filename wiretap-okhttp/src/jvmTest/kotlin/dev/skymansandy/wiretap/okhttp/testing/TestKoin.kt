/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.testing

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Boots a tiny Koin context with the given collaborators and installs it via
 * [WiretapDi.setTestKoin]. Return the [Koin] so the caller can resolve overrides; call
 * [WiretapDi.setTestKoin] with `null` in `@After` to restore production wiring.
 */
internal fun installTestKoin(
    httpLogManager: HttpLogManager,
    findMatchingRule: FindMatchingRuleUseCase,
    extras: Module = module { },
): Koin {
    val app = koinApplication {
        modules(
            module {
                single { httpLogManager }
                single { findMatchingRule }
            },
            extras,
        )
    }
    WiretapDi.setTestKoin(app.koin)
    return app.koin
}

internal fun teardownTestKoin() {
    WiretapDi.setTestKoin(null)
}
