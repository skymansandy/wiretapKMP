package dev.skymansandy.wiretap.domain.model

sealed class RuleAction {

    enum class Type { Mock, Throttle }

    abstract val type: Type

    val name: String get() = type.name

    data class Mock(
        val responseCode: Int = 200,
        val responseBody: String? = null,
        val responseHeaders: Map<String, String>? = null,
        val throttleDelayMs: Long? = null,
        val throttleDelayMaxMs: Long? = null,
    ) : RuleAction() {
        override val type: Type = Type.Mock
    }

    data class Throttle(
        val delayMs: Long = 0,
        val delayMaxMs: Long? = null,
    ) : RuleAction() {
        override val type: Type = Type.Throttle
    }
}
