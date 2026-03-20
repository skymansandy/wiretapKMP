package dev.skymansandy.wiretap.domain.model

sealed interface UrlMatcher {

    val pattern: String

    data class Exact(override val pattern: String) : UrlMatcher

    data class Contains(override val pattern: String) : UrlMatcher

    data class Regex(override val pattern: String) : UrlMatcher
}
