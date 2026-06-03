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

    /** How to render `Binary` frames in the log. Defaults to [BinaryFrameDecoding.Auto]. */
    var binaryDecoding: BinaryFrameDecoding = BinaryFrameDecoding.Auto
}
