package dev.skymansandy.wiretap.config

import kotlin.time.Duration

data class WiretapConfig(
    val enabled: Boolean = true,
    val loggingEnabled: Boolean = true,

    /**
     * Composable request filters. Only requests matching ALL filters are captured.
     * An empty list (default) captures everything.
     *
     * @see RequestFilter
     */
    val requestFilters: List<RequestFilter> = emptyList(),

    /**
     * Header sanitization rules applied to stored/displayed log data.
     * Original request/response objects are never mutated.
     *
     * @see HeaderSanitizationRule
     */
    val headerSanitizationRules: List<HeaderSanitizationRule> = emptyList(),

    /**
     * How long to retain log entries. Entries older than this duration are deleted
     * automatically on each new request capture. `null` (default) means no expiry.
     *
     * Cleanup uses a timestamp index — no full table scans.
     *
     * Example: `logRetentionDuration = 24.hours`
     */
    val logRetentionDuration: Duration? = null,
)
