/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.sse

import io.ktor.client.plugins.sse.ClientSSESession

/**
 * No-op: returns a passthrough wrapper for API parity.
 */
suspend fun ClientSSESession.wiretapped(): WiretapSseSession = DelegatingSseSession(this)
