/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.plugins.sse.ClientSSESession

@ExperimentalWiretapSseApi
@Deprecated(
    message = "WiretapKtorSsePlugin now wraps sessions automatically. Remove this call.",
    replaceWith = ReplaceWith("this"),
    level = DeprecationLevel.ERROR,
)
fun ClientSSESession.wiretapped(): ClientSSESession = this
