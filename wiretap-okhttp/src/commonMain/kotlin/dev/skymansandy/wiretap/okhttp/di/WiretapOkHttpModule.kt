/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.di

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.okhttp.RealOkHttpInterceptor
import dev.skymansandy.wiretap.okhttp.RealOkHttpWsListenerFactory
import dev.skymansandy.wiretap.okhttp.WiretapOkHttpInterceptorDelegate
import dev.skymansandy.wiretap.okhttp.WiretapOkHttpWsListenerFactory
import org.koin.core.module.Module
import org.koin.dsl.module

val wiretapOkHttpModule: Module = module {
    single<WiretapOkHttpInterceptorDelegate> { RealOkHttpInterceptor() }
    single<WiretapOkHttpWsListenerFactory> { RealOkHttpWsListenerFactory() }
}

/**
 * Registers the wiretap-okhttp Koin module into Wiretap's DI context.
 *
 * On Android, this is called automatically via App Startup ([WiretapOkHttpInitializer]).
 * On JVM Desktop, call this manually after app startup:
 * ```kotlin
 * WiretapOkHttp.initialize()
 * ```
 */
object WiretapOkHttp {

    fun initialize() {
        WiretapDi.loadModules(listOf(wiretapOkHttpModule))
    }
}
