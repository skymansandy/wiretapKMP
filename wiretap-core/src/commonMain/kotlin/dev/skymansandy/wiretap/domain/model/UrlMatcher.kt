package dev.skymansandy.wiretap.domain.model

sealed interface UrlMatcher {

    val pattern: String

    val type: MatcherType

    data class Exact(override val pattern: String) : UrlMatcher {
        override val type = MatcherType.Exact
    }

    data class Contains(override val pattern: String) : UrlMatcher {
        override val type = MatcherType.Contains
    }

    data class Regex(override val pattern: String) : UrlMatcher {
        override val type = MatcherType.Regex
    }
}