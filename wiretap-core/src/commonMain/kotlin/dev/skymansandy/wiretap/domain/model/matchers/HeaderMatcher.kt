/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.matchers

/**
 * Defines how a rule matches request headers.
 * Multiple matchers on a rule use AND logic — all must match.
 * Header key matching is case-insensitive.
 *
 * @see dev.skymansandy.wiretap.domain.model.WiretapRule
 */
sealed interface HeaderMatcher {

    val key: String

    /** Matches when a header with the given [key] exists, regardless of value. */
    data class KeyExists(override val key: String) : HeaderMatcher

    /** Matches when the header [key] exists and its value equals [value] exactly. */
    data class ValueExact(override val key: String, val value: String) : HeaderMatcher

    /** Matches when the header [key] exists and its value contains [value] as a substring. */
    data class ValueContains(override val key: String, val value: String) : HeaderMatcher

    /** Matches when the header [key] exists and its value matches [pattern] as a regex. */
    data class ValueRegex(override val key: String, val pattern: String) : HeaderMatcher
}
