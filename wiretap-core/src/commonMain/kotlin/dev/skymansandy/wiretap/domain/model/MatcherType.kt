package dev.skymansandy.wiretap.domain.model

enum class MatcherType {
    URL_EXACT,
    URL_REGEX,
    HEADER_CONTAINS,
    BODY_CONTAINS,
}
