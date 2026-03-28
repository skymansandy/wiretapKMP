package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {
    // no-op
}
