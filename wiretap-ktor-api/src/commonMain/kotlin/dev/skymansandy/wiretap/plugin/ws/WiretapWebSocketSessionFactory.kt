/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

/**
 * Factory for creating [WiretapWebSocketSession] instances.
 *
 * In debug builds, wiretap-ktor registers an implementation that creates
 * [LoggingWebSocketSession] instances. In release builds (API-only), no
 * factory is registered and [wiretapped] falls back to [DelegatingWebSocketSession].
 */
interface WiretapWebSocketSessionFactory {

    fun create(session: DefaultClientWebSocketSession): WiretapWebSocketSession
}
