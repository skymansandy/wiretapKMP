/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import io.ktor.client.plugins.api.createClientPlugin

val WiretapKtorSsePlugin = createClientPlugin("WiretapSsePlugin") {
    // no-op
}
