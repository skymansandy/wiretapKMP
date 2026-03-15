package dev.skymansandy.wiretap.domain.model

sealed class BodyMatcher {
    abstract val pattern: String

    data class Exact(override val pattern: String) : BodyMatcher()
    data class Contains(override val pattern: String) : BodyMatcher()
    data class Regex(override val pattern: String) : BodyMatcher()
}
