/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.ws

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession

@Deprecated(
    message = "WiretapKtorWebSocketPlugin now wraps sessions automatically. Remove this call.",
    replaceWith = ReplaceWith("this"),
    level = DeprecationLevel.ERROR,
)
fun DefaultClientWebSocketSession.wiretapped(): DefaultClientWebSocketSession = this
