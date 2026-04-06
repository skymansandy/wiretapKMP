/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.di

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.plugin.http.RealKtorHttpPlugin
import dev.skymansandy.wiretap.plugin.http.WiretapKtorHttpPluginDelegate
import dev.skymansandy.wiretap.plugin.ws.RealKtorWsPlugin
import dev.skymansandy.wiretap.plugin.ws.RealWebSocketSessionFactory
import dev.skymansandy.wiretap.plugin.ws.WiretapKtorWsPluginDelegate
import dev.skymansandy.wiretap.plugin.ws.WiretapWebSocketSessionFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val wiretapKtorModule: Module = module {
    single<WiretapKtorHttpPluginDelegate> { RealKtorHttpPlugin() }
    single<WiretapKtorWsPluginDelegate> { RealKtorWsPlugin() }
    single<WiretapWebSocketSessionFactory> { RealWebSocketSessionFactory() }
}

/**
 * Registers the wiretap-ktor Koin module into Wiretap's DI context.
 *
 * On Android, this is called automatically via App Startup ([WiretapKtorInitializer]).
 * On JVM Desktop and iOS, call this manually after app startup:
 * ```kotlin
 * WiretapKtor.initialize()
 * ```
 */
object WiretapKtor {

    fun initialize() {
        WiretapDi.loadModules(listOf(wiretapKtorModule))
    }
}
