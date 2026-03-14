package dev.skymansandy.wiretap.domain.model

sealed class UrlMatcher {
    abstract val pattern: String

    data class Exact(override val pattern: String) : UrlMatcher()
    data class Contains(override val pattern: String) : UrlMatcher()
    data class Regex(override val pattern: String) : UrlMatcher()
}
