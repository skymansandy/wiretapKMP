/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import io.ktor.client.plugins.api.createClientPlugin

/**
 * Ktor client plugin placeholder for Wiretap SSE inspection.
 *
 * SSE connection tracking is handled by [wiretapped] extension on [ClientSSESession].
 * This plugin is kept for API consistency but performs no interception.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(SSE)
 *     install(WiretapKtorSsePlugin)
 * }
 * ```
 */
val WiretapKtorSsePlugin = createClientPlugin("WiretapSsePlugin") {
    // No-op: connection creation is handled in ClientSSESession.wiretapped()
}
