/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.api.ClientPluginBuilder

/**
 * Delegate interface for the Ktor WebSocket plugin.
 *
 * Implemented by wiretap-ktor's [RealKtorWsPlugin] and registered in Koin.
 */
interface WiretapKtorWsPluginDelegate {

    fun ClientPluginBuilder<Unit>.install()
}
