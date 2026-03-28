package dev.skymansandy.wiretap.plugin.http

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorHttpPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    // no-op
}
