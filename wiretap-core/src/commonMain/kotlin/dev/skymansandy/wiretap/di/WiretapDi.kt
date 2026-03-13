package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object WiretapDi : KoinComponent {

    val orchestrator: WiretapOrchestrator by inject()
    val ruleRepository: RuleRepository by inject()

    val koinModule get() = wiretapModule
}
