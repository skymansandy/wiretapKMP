/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

/**
 * No-op — returns a passthrough [WiretapWebSocketSession] that delegates
 * directly to the underlying session without any logging.
 */
fun DefaultClientWebSocketSession.wiretapped(): WiretapWebSocketSession {
    val session = this
    return DelegatingWebSocketSession(session)
}
