package dev.skymansandy.wiretap.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

object WiretapDi : KoinComponent {

    override fun getKoin(): Koin = WiretapKoinContext.koin

    /**
     * Override the internal Koin context for testing.
     * Pass `null` to restore the production context.
     */
    fun setTestKoin(koin: Koin?) {
        WiretapKoinContext.setTestKoin(koin)
    }
}
