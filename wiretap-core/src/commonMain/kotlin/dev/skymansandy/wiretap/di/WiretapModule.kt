package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.HttpOrchestratorImpl
import dev.skymansandy.wiretap.domain.orchestrator.SocketOrchestratorImpl
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestratorImpl
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import org.koin.dsl.module

val wiretapModule = module {

    includes(wiretapDataModule)
    includes(wiretapUtilityModule)

    single<WiretapOrchestrator> {
        WiretapOrchestratorImpl(
            httpOrchestrator = HttpOrchestratorImpl(
                httpRepository = get(),
                wiretapLogger = get(),
            ),
            socketOrchestrator = SocketOrchestratorImpl(
                socketRepository = get(),
                wiretapLogger = get(),
            ),
        )
    }

    single { FindMatchingRuleUseCase(ruleRepository = get()) }

    single { FindConflictingRulesUseCase(ruleRepository = get()) }
}
