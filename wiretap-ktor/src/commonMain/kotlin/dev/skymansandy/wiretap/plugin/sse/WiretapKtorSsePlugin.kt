/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.config.sse.WiretapSseConfig
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.AttributeKey

internal val WiretapSseEnabledKey = AttributeKey<Boolean>("WiretapSseEnabled")

/**
 * Ktor client plugin for Wiretap SSE inspection.
 *
 * SSE connection tracking is handled by the [wiretapped] extension on [ClientSSESession].
 * This plugin stores the [WiretapSseConfig] so that [wiretapped] can read it.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(SSE)
 *     install(WiretapKtorSsePlugin) {
 *         enabled = true
 *     }
 * }
 * ```
 */
@ExperimentalWiretapSseApi
val WiretapKtorSsePlugin = createClientPlugin("WiretapSsePlugin", ::WiretapSseConfig) {
    val enabled = pluginConfig.enabled

    onRequest { request, _ ->
        request.attributes.put(WiretapSseEnabledKey, enabled)
    }
}
