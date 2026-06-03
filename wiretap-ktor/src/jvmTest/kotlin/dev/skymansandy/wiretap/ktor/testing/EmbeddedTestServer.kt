/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ktor.testing

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.runBlocking

/**
 * Boots a minimal Ktor CIO server with WebSocket + SSE support on a random
 * port. Supply a routing block; call [stop] when done. Use [port] to reach
 * the running server.
 */
internal class EmbeddedTestServer(routes: Routing.() -> Unit) {
    private val server: EmbeddedServer<*, *> = embeddedServer(CIO, port = 0) {
        install(WebSockets)
        install(SSE)
        routing(routes)
    }.start(wait = false)

    val port: Int = runBlocking { server.engine.resolvedConnectors().first().port }

    fun stop() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = 0)
    }
}
