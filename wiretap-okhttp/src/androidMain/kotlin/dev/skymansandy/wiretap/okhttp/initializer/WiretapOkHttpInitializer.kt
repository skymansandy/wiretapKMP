/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.initializer

import android.content.Context
import androidx.startup.Initializer
import dev.skymansandy.wiretap.helper.initializer.WiretapInitializer
import dev.skymansandy.wiretap.okhttp.di.WiretapOkHttp

/**
 * App Startup initializer that registers wiretap-okhttp's Koin module.
 *
 * Depends on [WiretapInitializer] to ensure Wiretap's core Koin context
 * is initialized before the okhttp delegates are registered.
 */
internal class WiretapOkHttpInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        WiretapOkHttp.initialize()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        WiretapInitializer::class.java,
    )
}
