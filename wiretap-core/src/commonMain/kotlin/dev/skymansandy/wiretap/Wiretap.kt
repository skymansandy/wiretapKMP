package dev.skymansandy.wiretap

import dev.skymansandy.wiretap.di.wiretapModule
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.repository.RuleRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Wiretap : KoinComponent {

    val orchestrator: WiretapOrchestrator by inject()
    val ruleRepository: RuleRepository by inject()

    val koinModule get() = wiretapModule
}
