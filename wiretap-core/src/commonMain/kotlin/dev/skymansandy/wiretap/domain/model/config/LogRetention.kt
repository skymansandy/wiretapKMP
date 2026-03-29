/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config

/**
 * Controls how long network logs are retained.
 *
 * @see WiretapConfig.logRetention
 */
sealed interface LogRetention {

    /** Logs are kept indefinitely (default). */
    object Forever : LogRetention

    /**
     * Logs are cleared when the plugin first initializes (i.e., at app startup).
     * Only logs from the current session are visible.
     */
    object AppSession : LogRetention

    /**
     * Logs older than [days] days are pruned on each new request capture.
     * Uses an indexed timestamp query — no full table scans.
     */
    data class Days(val days: Int) : LogRetention
}
