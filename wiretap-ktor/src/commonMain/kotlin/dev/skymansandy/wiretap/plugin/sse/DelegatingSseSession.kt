/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalWiretapSseApi::class)
@Deprecated(
    message = "WiretapKtorSsePlugin now wraps sessions automatically. Use ClientSSESession directly.",
    level = DeprecationLevel.ERROR,
)
@Suppress("DEPRECATION_ERROR")
internal class DelegatingSseSession(
    private val delegate: ClientSSESession,
) : WiretapSseSession {

    override val call: HttpClientCall = delegate.call

    override val incoming: Flow<ServerSentEvent> = delegate.incoming
}
