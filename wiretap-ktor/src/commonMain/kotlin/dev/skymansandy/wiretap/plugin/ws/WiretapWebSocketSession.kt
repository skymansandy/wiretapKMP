package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.call.HttpClientCall
import io.ktor.websocket.DefaultWebSocketSession

/**
 * Wraps a WebSocket session for Wiretap interception.
 *
 * Exposes the full [DefaultWebSocketSession] surface (incoming, outgoing,
 * send, flush, closeReason, etc.) plus [call], so it can be used as a
 * drop-in replacement for `DefaultClientWebSocketSession`.
 *
 * In debug builds, logs all sent/received frames.
 * In noop builds, delegates directly to the underlying session.
 */
interface WiretapWebSocketSession : DefaultWebSocketSession {

    val call: HttpClientCall
}
