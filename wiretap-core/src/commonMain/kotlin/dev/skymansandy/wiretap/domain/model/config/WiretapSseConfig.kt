/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi

/**
 * Configuration for Wiretap SSE inspection plugins.
 */
@ExperimentalWiretapSseApi
class WiretapSseConfig {
    /** Master switch. When `false`, [wiretapped] returns a passthrough session without logging. */
    var enabled: Boolean = true
}
