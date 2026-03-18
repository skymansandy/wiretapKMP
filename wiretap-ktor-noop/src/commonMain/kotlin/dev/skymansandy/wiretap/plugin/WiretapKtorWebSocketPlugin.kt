package dev.skymansandy.wiretap.plugin

import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {
    // no-op
}
