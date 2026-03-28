package dev.skymansandy.wiretap.domain.model.matchers

import dev.skymansandy.wiretap.domain.model.MatcherType

/**
 * Defines how a rule matches the request body.
 *
 * @see dev.skymansandy.wiretap.domain.model.WiretapRule
 */
sealed interface BodyMatcher {

    val pattern: String
    val type: MatcherType

    /** Matches when the request body equals [pattern] exactly. */
    data class Exact(override val pattern: String) : BodyMatcher {
        override val type = MatcherType.Exact
    }

    /** Matches when the request body contains [pattern] as a substring. */
    data class Contains(override val pattern: String) : BodyMatcher {
        override val type = MatcherType.Contains
    }

    /** Matches when the request body matches [pattern] as a regular expression. */
    data class Regex(override val pattern: String) : BodyMatcher {
        override val type = MatcherType.Regex
    }
}
