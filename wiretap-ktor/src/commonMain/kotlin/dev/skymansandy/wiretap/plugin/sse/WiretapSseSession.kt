/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.call.HttpClientCall
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow

@ExperimentalWiretapSseApi
@Deprecated(
    message = "WiretapKtorSsePlugin now wraps sessions automatically. Use ClientSSESession directly.",
    level = DeprecationLevel.ERROR,
)
interface WiretapSseSession {

    val call: HttpClientCall

    val incoming: Flow<ServerSentEvent>
}
