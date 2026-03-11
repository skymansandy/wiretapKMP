package dev.skymansandy.wiretap.plugin

import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorPlugin = createClientPlugin("WiretapPlugin") {
    // no-op
}
