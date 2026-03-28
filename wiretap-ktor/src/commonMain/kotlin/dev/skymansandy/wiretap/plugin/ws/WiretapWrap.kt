package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.plugin.ws.util.WsPluginDeps
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

/**
 * Extension to wrap a Ktor [DefaultClientWebSocketSession] for Wiretap logging.
 *
 * Requires [WiretapKtorWebSocketPlugin] to be installed in the HttpClient.
 * Returns a passthrough if the plugin is not installed.
 *
 * ```kotlin
 * client.webSocket("wss://example.com/ws") {
 *     val session = this.wiretapped()
 *     session.send(Frame.Text("hello"))
 *     for (frame in session.incoming) { ... }
 * }
 * ```
 */
fun DefaultClientWebSocketSession.wiretapped(): WiretapWebSocketSession {
    val socketId = call.request.attributes.getOrNull(WiretapSocketIdKey) ?: return DelegatingWebSocketSession(this)
    if (socketId < 0) return DelegatingWebSocketSession(this)

    return LoggingWebSocketSession(
        delegate = this,
        socketId = socketId,
        socketLogManager = WsPluginDeps().socketLogManager,
    )
}
