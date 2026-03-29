/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {
    // no-op
}
