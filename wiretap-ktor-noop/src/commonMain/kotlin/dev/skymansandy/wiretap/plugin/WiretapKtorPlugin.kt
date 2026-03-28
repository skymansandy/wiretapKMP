package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    // no-op
}
