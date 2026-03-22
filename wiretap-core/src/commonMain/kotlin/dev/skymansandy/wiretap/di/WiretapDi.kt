package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object WiretapDi : KoinComponent {

    override fun getKoin(): Koin = WiretapKoinContext.koin

    val orchestrator: WiretapOrchestrator by inject()

    val ruleRepository: RuleRepository by inject()

    val findConflictingRules: FindConflictingRulesUseCase by inject()

    /**
     * Override the internal Koin context for testing.
     * Pass `null` to restore the production context.
     */
    fun setTestKoin(koin: Koin?) {
        WiretapKoinContext.setTestKoin(koin)
    }
}
