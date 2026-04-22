/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.call.HttpClientCall
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow

/**
 * Wraps an SSE session for Wiretap interception.
 *
 * In debug builds, logs all incoming SSE events.
 * In noop builds, delegates directly to the underlying session.
 */
@ExperimentalWiretapSseApi
interface WiretapSseSession {

    val call: HttpClientCall

    val incoming: Flow<ServerSentEvent>
}
