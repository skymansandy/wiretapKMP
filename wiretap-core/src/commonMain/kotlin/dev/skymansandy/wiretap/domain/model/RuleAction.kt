/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

/**
 * Action to perform when a [WiretapRule][WiretapRule] matches a request.
 *
 * @see Mock
 * @see Throttle
 * @see MockAndThrottle
 */
sealed interface RuleAction {

    enum class Type(val label: String) {

        Mock("Mock"),
        Throttle("Throttle"),
        MockAndThrottle("Mock + Throttle"),
    }

    val type: Type

    val name: String get() = type.name

    /**
     * Returns a fake response without hitting the network.
     *
     * @property responseCode HTTP status code for the mock response (default 200).
     * @property responseBody Optional response body string.
     * @property responseHeaders Optional response headers.
     */
    data class Mock(
        val responseCode: Int = 200,
        val responseBody: String? = null,
        val responseHeaders: Map<String, String>? = null,
    ) : RuleAction {

        override val type: Type = Type.Mock
    }

    /**
     * Delays the request before proceeding to the real network.
     *
     * @property delayMs Minimum delay in milliseconds.
     * @property delayMaxMs If set, delay is randomized between [delayMs] and this value.
     */
    data class Throttle(
        val delayMs: Long = 0,
        val delayMaxMs: Long? = null,
    ) : RuleAction {

        override val type: Type = Type.Throttle
    }

    /**
     * Delays the request, then returns a fake response without hitting the network.
     *
     * Combines [Mock] and [Throttle] — the throttle delay is required.
     *
     * @property responseCode HTTP status code for the mock response (default 200).
     * @property responseBody Optional response body string.
     * @property responseHeaders Optional response headers.
     * @property delayMs Minimum delay in milliseconds before returning the mock.
     * @property delayMaxMs If set, delay is randomized between [delayMs] and this value.
     */
    data class MockAndThrottle(
        val responseCode: Int = 200,
        val responseBody: String? = null,
        val responseHeaders: Map<String, String>? = null,
        val delayMs: Long = 1000,
        val delayMaxMs: Long? = null,
    ) : RuleAction {

        override val type: Type = Type.MockAndThrottle
    }
}
