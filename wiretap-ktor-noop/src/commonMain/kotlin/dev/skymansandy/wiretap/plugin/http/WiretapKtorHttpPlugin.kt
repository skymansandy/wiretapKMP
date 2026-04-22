/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

import dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig
import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorHttpPlugin = createClientPlugin("WiretapPlugin", ::WiretapHttpConfig) {
    // no-op
}
