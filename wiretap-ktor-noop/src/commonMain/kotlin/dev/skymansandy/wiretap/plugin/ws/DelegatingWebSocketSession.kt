/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.DefaultWebSocketSession

/**
 * Passthrough [WiretapWebSocketSession] that delegates directly
 * to the underlying session without any logging.
 */
internal class DelegatingWebSocketSession(
    delegate: DefaultClientWebSocketSession,
) : WiretapWebSocketSession, DefaultWebSocketSession by delegate {

    override val call: HttpClientCall = delegate.call
}
