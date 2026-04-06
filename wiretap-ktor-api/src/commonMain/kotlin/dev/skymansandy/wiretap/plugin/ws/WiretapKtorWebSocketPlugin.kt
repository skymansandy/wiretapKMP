/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.di.WiretapDi
import io.ktor.client.plugins.api.createClientPlugin
import org.koin.core.component.KoinComponent

/**
 * Ktor client plugin that intercepts WebSocket sessions to log
 * connections and messages via Wiretap.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(WebSockets)
 *     install(WiretapKtorWebSocketPlugin)
 * }
 * ```
 *
 * In debug builds (with wiretap-ktor on the classpath), real logging activates.
 * In release builds (wiretap-ktor-noop or API-only), this is a no-op.
 */
val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {
    val delegate = try {
        object : KoinComponent {
            override fun getKoin() = WiretapDi.getKoin()
        }.getKoin().getOrNull<WiretapKtorWsPluginDelegate>()
    } catch (_: Exception) {
        null
    }

    if (delegate != null) {
        with(delegate) { install() }
    }
}
