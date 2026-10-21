/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.ws

/**
 * Configuration for Wiretap WebSocket inspection plugins.
 */
class WiretapWsConfig {
    /** Master switch. When `false`, the plugin skips logging and [wiretapped] returns a passthrough session. */
    var enabled: Boolean = true

    /**
     * Whether this plugin mirrors its captured connections and frames to the platform console —
     * logcat on Android, the Xcode console on iOS, stdout on JVM. Set to `false` to stop a chatty
     * socket from drowning out your log while still capturing it. Defaults to `true`.
     *
     * This has no effect on capture: every connection and frame is still stored and browsable in
     * the inspector.
     */
    var consoleLogging: Boolean = true

    /** How to render `Binary` frames in the log. Defaults to [BinaryFrameDecoding.Auto]. */
    var binaryDecoding: BinaryFrameDecoding = BinaryFrameDecoding.Auto
}
