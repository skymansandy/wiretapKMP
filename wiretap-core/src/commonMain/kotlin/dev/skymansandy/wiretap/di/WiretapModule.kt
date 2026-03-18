package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestratorImpl
import org.koin.dsl.module

val wiretapModule = module {

    includes(wiretapDataModule)
    includes(wiretapUtilityModule)

    single<WiretapOrchestrator> {
        WiretapOrchestratorImpl(
            config = get(),
            networkRepository = get(),
            socketRepository = get(),
            networkLogger = get(),
        )
    }
}
