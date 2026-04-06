/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.module.Module

object WiretapDi : KoinComponent {

    override fun getKoin(): Koin = WiretapKoinContext.koin

    /**
     * Register additional Koin modules into Wiretap's DI context.
     * Used by plugin modules (wiretap-ktor, wiretap-okhttp) to register
     * their delegate implementations after core's Koin is initialized.
     */
    fun loadModules(modules: List<Module>) {
        WiretapKoinContext.loadModules(modules)
    }

    /**
     * Override the internal Koin context for testing.
     * Pass `null` to restore the production context.
     */
    fun setTestKoin(koin: Koin?) {
        WiretapKoinContext.setTestKoin(koin)
    }
}
