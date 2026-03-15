package dev.skymansandy.wiretap.domain.model

sealed class HeaderMatcher {
    abstract val key: String

    data class KeyExists(override val key: String) : HeaderMatcher()
    data class ValueExact(override val key: String, val value: String) : HeaderMatcher()
    data class ValueContains(override val key: String, val value: String) : HeaderMatcher()
    data class ValueRegex(override val key: String, val pattern: String) : HeaderMatcher()
}
