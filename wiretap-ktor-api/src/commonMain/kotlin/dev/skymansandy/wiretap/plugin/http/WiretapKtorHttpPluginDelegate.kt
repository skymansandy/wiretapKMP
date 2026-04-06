/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import io.ktor.client.plugins.api.ClientPluginBuilder

/**
 * Delegate interface for the Ktor HTTP plugin.
 *
 * Implemented by wiretap-ktor's [RealKtorHttpPlugin] and registered in Koin.
 * The API module's [WiretapKtorHttpPlugin] resolves this at runtime — if found,
 * real logging/mocking activates; if not, the plugin is a no-op.
 */
interface WiretapKtorHttpPluginDelegate {

    fun ClientPluginBuilder<WiretapConfig>.install()
}
