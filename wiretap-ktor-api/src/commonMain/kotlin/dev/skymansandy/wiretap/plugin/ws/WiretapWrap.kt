/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.di.WiretapDi
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import org.koin.core.component.KoinComponent

/**
 * Extension to wrap a Ktor [DefaultClientWebSocketSession] for Wiretap logging.
 *
 * Requires [WiretapKtorWebSocketPlugin] to be installed in the HttpClient.
 * Returns a passthrough if the plugin is not installed or no factory is registered.
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
    val factory = try {
        object : KoinComponent {
            override fun getKoin() = WiretapDi.getKoin()
        }.getKoin().getOrNull<WiretapWebSocketSessionFactory>()
    } catch (_: Exception) {
        null
    }

    return factory?.create(this) ?: DelegatingWebSocketSession(this)
}
