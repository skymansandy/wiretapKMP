package dev.skymansandy.wiretap.domain.model.matchers

import dev.skymansandy.wiretap.domain.model.MatcherType

/**
 * Defines how a rule's URL pattern is matched against request URLs.
 *
 * All matching is case-insensitive.
 *
 * @see dev.skymansandy.wiretap.domain.model.WiretapRule
 */
sealed interface UrlMatcher {

    val pattern: String

    val type: MatcherType

    /** Matches when the request URL equals [pattern] exactly (case-insensitive). */
    data class Exact(override val pattern: String) : UrlMatcher {

        override val type = MatcherType.Exact
    }

    /** Matches when the request URL contains [pattern] as a substring (case-insensitive). */
    data class Contains(override val pattern: String) : UrlMatcher {

        override val type = MatcherType.Contains
    }

    /** Matches when the request URL matches [pattern] as a regular expression. */
    data class Regex(override val pattern: String) : UrlMatcher {

        override val type = MatcherType.Regex
    }
}
