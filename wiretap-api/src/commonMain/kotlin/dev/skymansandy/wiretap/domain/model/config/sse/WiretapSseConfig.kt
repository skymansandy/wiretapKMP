/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.sse

import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi

/**
 * Configuration for Wiretap SSE inspection plugins.
 */
@ExperimentalWiretapSseApi
class WiretapSseConfig {
    /** Master switch. When `false`, [wiretapped] returns a passthrough session without logging. */
    var enabled: Boolean = true

    /**
     * Whether this plugin mirrors its captured connections and events to the platform console —
     * logcat on Android, the Xcode console on iOS, stdout on JVM. Defaults to `true`.
     *
     * Set to `false` to keep a high-frequency event stream out of your log while still
     * capturing it. This has no effect on capture: connections and events are always stored
     * and browsable in the inspector.
     */
    var consoleLogging: Boolean = true
}
