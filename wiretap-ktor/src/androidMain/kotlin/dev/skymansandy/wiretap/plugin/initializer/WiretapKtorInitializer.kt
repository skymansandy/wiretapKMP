/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.initializer

import android.content.Context
import androidx.startup.Initializer
import dev.skymansandy.wiretap.helper.initializer.WiretapInitializer
import dev.skymansandy.wiretap.plugin.di.WiretapKtor

/**
 * App Startup initializer that registers wiretap-ktor's Koin module.
 *
 * Depends on [WiretapInitializer] to ensure Wiretap's core Koin context
 * is initialized before the ktor delegates are registered.
 */
internal class WiretapKtorInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        WiretapKtor.initialize()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        WiretapInitializer::class.java,
    )
}
