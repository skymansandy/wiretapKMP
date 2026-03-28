package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import dev.skymansandy.wiretap.plugin.ws.util.WsPluginDeps
import dev.skymansandy.wiretap.plugin.ws.util.toWebSocketUrl
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.AttributeKey

internal val WiretapSocketIdKey = AttributeKey<Long>("WiretapSocketId")

/**
 * Ktor client plugin that intercepts WebSocket sessions to log
 * connections and messages via Wiretap.
 *
 * Usage:
 * ```kotlin
 * HttpClient {
 *     install(WebSockets)
 *     install(WiretapKtorWebSocketPlugin)
 * }
 * ```
 *
 * Note: This plugin hooks into 101 Switching Protocols responses.
 * For full outgoing message interception, use [WiretapWebSocketSession]
 * to wrap your session.
 */
val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {

    val deps = WsPluginDeps()

    onResponse { response ->
        // Only intercept WebSocket upgrades (status 101)
        if (response.status.value != 101) return@onResponse

        val url = response.call.request.url.toString().toWebSocketUrl()
        val requestHeaders = response.call.request.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }

        val socketId = deps.socketLogManager.createSocket(
            SocketConnection(
                url = url,
                requestHeaders = requestHeaders,
                status = SocketStatus.Open,
                timestamp = currentTimeMillis(),
                protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" },
            ),
        )

        // Store socket ID for later use
        response.call.request.attributes.put(WiretapSocketIdKey, socketId)
    }
}
