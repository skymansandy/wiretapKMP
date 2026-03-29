/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig.Companion.MAX_CONTENT_LENGTH

/**
 * Configuration for Wiretap network inspection plugins.
 *
 * Each plugin (Ktor, OkHttp, NSURLSession) accepts this config at installation/construction time.
 * All properties are mutable so the class can be used directly as a Ktor DSL config block.
 *
 * Example — Ktor DSL:
 * ```kotlin
 * install(WiretapKtorHttpPlugin) {
 *     shouldLog = { url, _ -> url.contains("/api/") }
 *     headerAction = { key ->
 *         when {
 *             key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
 *             key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
 *             else -> HeaderAction.Keep
 *         }
 *     }
 *     logRetention = LogRetention.Days(7)
 * }
 * ```
 *
 * Example — OkHttp / NSURLSession builder:
 * ```kotlin
 * WiretapOkHttpInterceptor {
 *     enabled = false
 * }
 * ```
 */
class WiretapConfig {

    /** Master switch. When `false`, the plugin passes requests through without any logging. */
    var enabled: Boolean = true

    /**
     * Return `true` to capture the request, `false` to skip it entirely.
     * Evaluated before any DB write. Defaults to capturing everything.
     */
    var shouldLog: (url: String, method: String) -> Boolean = { _, _ -> true }

    /**
     * Called for every header key in both request and response before logging.
     * Return the desired [HeaderAction] for that key. Defaults to [HeaderAction.Keep].
     * The original request/response objects are never mutated.
     */
    var headerAction: (key: String) -> HeaderAction = { HeaderAction.Keep }

    /**
     * How long log entries are retained. Defaults to [LogRetention.Forever].
     * [LogRetention.AppSession] clears all existing logs when the plugin first initializes.
     * [LogRetention.Days] prunes entries older than N days on each new capture.
     */
    var logRetention: LogRetention = LogRetention.Forever

    /**
     * Maximum number of characters to store for request and response bodies.
     * Bodies exceeding this limit are truncated before being saved to the database.
     * Capped at [MAX_CONTENT_LENGTH] (500 KB). Defaults to [MAX_CONTENT_LENGTH].
     * Set to 0 to skip body logging entirely.
     */
    var maxContentLength: Int = MAX_CONTENT_LENGTH
        set(value) {
            field = value.coerceIn(0, MAX_CONTENT_LENGTH)
        }

    companion object {
        /** Hard upper bound: 250 KB expressed in characters. */
        const val MAX_CONTENT_LENGTH: Int = 500 * 1024
    }
}
