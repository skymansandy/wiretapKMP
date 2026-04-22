/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.domain.model.config.sse.WiretapSseConfig
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.plugins.api.createClientPlugin

@ExperimentalWiretapSseApi
val WiretapKtorSsePlugin = createClientPlugin("WiretapSsePlugin", ::WiretapSseConfig) {
    // no-op
}
