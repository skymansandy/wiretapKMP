package dev.skymansandy.wiretap.domain.model

sealed interface BodyMatcher {

    val pattern: String

    data class Exact(override val pattern: String) : BodyMatcher

    data class Contains(override val pattern: String) : BodyMatcher

    data class Regex(override val pattern: String) : BodyMatcher
}
