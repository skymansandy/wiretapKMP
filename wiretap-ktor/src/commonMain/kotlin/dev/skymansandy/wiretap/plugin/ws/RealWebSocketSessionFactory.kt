/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.plugin.ws.util.WsPluginDeps
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

/**
 * Real implementation of [WiretapWebSocketSessionFactory] that creates
 * [LoggingWebSocketSession] instances for message interception.
 */
internal class RealWebSocketSessionFactory : WiretapWebSocketSessionFactory {

    override fun create(session: DefaultClientWebSocketSession): WiretapWebSocketSession {
        val socketId = session.call.request.attributes.getOrNull(WiretapSocketIdKey)
            ?: return DelegatingWebSocketSession(session)
        if (socketId < 0) return DelegatingWebSocketSession(session)

        return LoggingWebSocketSession(
            delegate = session,
            socketId = socketId,
            socketLogManager = WsPluginDeps().socketLogManager,
        )
    }
}
