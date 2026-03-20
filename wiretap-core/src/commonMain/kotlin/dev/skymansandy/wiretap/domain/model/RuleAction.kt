package dev.skymansandy.wiretap.domain.model

/**
 * Action to perform when a [WiretapRule][dev.skymansandy.wiretap.data.db.entity.WiretapRule] matches a request.
 *
 * @see Mock
 * @see Throttle
 */
sealed class RuleAction {

    enum class Type { Mock, Throttle }

    abstract val type: Type

    val name: String get() = type.name

    /**
     * Returns a fake response without hitting the network.
     *
     * @property responseCode HTTP status code for the mock response (default 200).
     * @property responseBody Optional response body string.
     * @property responseHeaders Optional response headers.
     * @property throttleDelayMs Optional delay before returning the mock (simulates latency).
     * @property throttleDelayMaxMs If set, delay is randomized between [throttleDelayMs] and this value.
     */
    data class Mock(
        val responseCode: Int = 200,
        val responseBody: String? = null,
        val responseHeaders: Map<String, String>? = null,
        val throttleDelayMs: Long? = null,
        val throttleDelayMaxMs: Long? = null,
    ) : RuleAction() {
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
    ) : RuleAction() {
        override val type: Type = Type.Throttle
    }
}
