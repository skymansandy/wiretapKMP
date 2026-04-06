/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import io.ktor.client.plugins.api.createClientPlugin
import org.koin.core.component.KoinComponent

/**
 * Ktor client plugin for Wiretap network inspection.
 *
 * Intercepts HTTP requests and responses to log them via the Wiretap orchestrator.
 * Supports mock and throttle rules — matching requests can return fake responses
 * or be delayed before reaching the network.
 *
 * Install in your [io.ktor.client.HttpClient] configuration:
 * ```kotlin
 * HttpClient {
 *     install(WiretapKtorHttpPlugin) {
 *         shouldLog = { url, _ -> url.contains("/api/") }
 *         headerAction = { key ->
 *             if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
 *             else HeaderAction.Keep
 *         }
 *         logRetention = LogRetention.Days(7)
 *     }
 * }
 * ```
 *
 * In debug builds (with wiretap-ktor on the classpath), real logging activates.
 * In release builds (wiretap-ktor-noop or API-only), this is a no-op.
 *
 * WebSocket upgrade requests (101) are skipped — use [dev.skymansandy.wiretap.plugin.ws.WiretapKtorWebSocketPlugin] for those.
 *
 * @see WiretapConfig
 * @see dev.skymansandy.wiretap.plugin.ws.WiretapKtorWebSocketPlugin
 */
val WiretapKtorHttpPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    val delegate = try {
        object : KoinComponent {
            override fun getKoin() = WiretapDi.getKoin()
        }.getKoin().getOrNull<WiretapKtorHttpPluginDelegate>()
    } catch (_: Exception) {
        null
    }

    if (delegate != null) {
        with(delegate) { install() }
    }
    // else: no-op — no delegate registered
}
