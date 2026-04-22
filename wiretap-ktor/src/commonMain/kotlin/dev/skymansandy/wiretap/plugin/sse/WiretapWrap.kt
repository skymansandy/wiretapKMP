/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.plugin.sse.util.SsePluginDeps
import io.ktor.client.plugins.sse.ClientSSESession

/**
 * Extension to wrap a Ktor [ClientSSESession] for Wiretap logging.
 *
 * Creates an SSE connection entry in Wiretap and returns a logging wrapper
 * that intercepts incoming events.
 *
 * ```kotlin
 * client.sse("https://example.com/events") {
 *     val session = this.wiretapped()
 *     session.incoming.collect { event -> ... }
 * }
 * ```
 */
@ExperimentalWiretapSseApi
suspend fun ClientSSESession.wiretapped(): WiretapSseSession {
    val enabled = call.request.attributes.getOrNull(WiretapSseEnabledKey) ?: true
    if (!enabled) return DelegatingSseSession(this)

    val deps = SsePluginDeps()
    val url = call.request.url.toString()
    val requestHeaders = call.request.headers.entries()
        .associate { (key, values) -> key to values.joinToString(", ") }

    val connectionId = deps.sseLogManager.createConnection(
        SseConnection(
            url = url,
            requestHeaders = requestHeaders,
            status = SseStatus.Open,
            timestamp = currentTimeMillis(),
        ),
    )

    return LoggingSseSession(
        delegate = this,
        connectionId = connectionId,
        sseLogManager = deps.sseLogManager,
    )
}
