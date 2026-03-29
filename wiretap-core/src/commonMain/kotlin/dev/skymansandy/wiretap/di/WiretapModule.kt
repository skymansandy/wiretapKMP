/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManagerImpl
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManagerImpl
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import org.koin.dsl.module

internal val wiretapModule = module {

    includes(wiretapDataModule)
    includes(wiretapUtilityModule)
    includes(wiretapViewModelModule)

    single<HttpLogManager> {
        HttpLogManagerImpl(
            httpRepository = get(),
            wiretapLogger = get(),
        )
    }

    single<SocketLogManager> {
        SocketLogManagerImpl(
            socketRepository = get(),
            wiretapLogger = get(),
        )
    }

    single {
        FindMatchingRuleUseCase(
            ruleRepository = get(),
        )
    }

    single {
        FindConflictingRulesUseCase(
            ruleRepository = get(),
        )
    }
}
