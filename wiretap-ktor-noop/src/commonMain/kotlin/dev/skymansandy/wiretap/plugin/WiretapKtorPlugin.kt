package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.config.WiretapConfig
import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    // no-op
}
