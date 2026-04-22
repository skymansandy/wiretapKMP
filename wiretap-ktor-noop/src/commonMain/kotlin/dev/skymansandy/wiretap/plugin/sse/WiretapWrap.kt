/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import io.ktor.client.plugins.sse.ClientSSESession

/**
 * No-op: returns a passthrough wrapper for API parity.
 */
@ExperimentalWiretapSseApi
suspend fun ClientSSESession.wiretapped(): WiretapSseSession = DelegatingSseSession(this)
