/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManagerImpl
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManagerImpl
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SseLogManagerImpl
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCaseImpl
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCaseImpl
import dev.skymansandy.wiretap.helper.launcher.DefaultWiretapNotificationHook
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import org.koin.dsl.module

internal val wiretapModule = module {

    includes(wiretapDataModule)
    includes(wiretapUtilityModule)
    includes(wiretapViewModelModule)

    single<WiretapNotificationHook> { DefaultWiretapNotificationHook }

    single<HttpLogManager> {
        HttpLogManagerImpl(
            httpRepository = get(),
            wiretapLogger = get(),
            notificationHook = get(),
        )
    }

    single<SocketLogManager> {
        SocketLogManagerImpl(
            socketRepository = get(),
            wiretapLogger = get(),
            notificationHook = get(),
        )
    }

    single<SseLogManager> {
        SseLogManagerImpl(
            sseRepository = get(),
            wiretapLogger = get(),
            notificationHook = get(),
        )
    }

    single<FindMatchingRuleUseCase> {
        FindMatchingRuleUseCaseImpl(
            ruleRepository = get(),
        )
    }

    single<FindConflictingRulesUseCase> {
        FindConflictingRulesUseCaseImpl(
            ruleRepository = get(),
        )
    }
}
