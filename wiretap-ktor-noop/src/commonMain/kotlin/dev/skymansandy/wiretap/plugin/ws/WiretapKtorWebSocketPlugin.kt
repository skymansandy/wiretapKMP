/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import dev.skymansandy.wiretap.domain.model.config.ws.WiretapWsConfig
import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin", ::WiretapWsConfig) {
    // no-op
}
