package dev.skymansandy.wiretap.ui.rules.model

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher

internal fun UrlMatchMode.label() = when (this) {
    UrlMatchMode.EXACT -> "Exact"
    UrlMatchMode.CONTAINS -> "Contains"
    UrlMatchMode.REGEX -> "Regex"
}

internal fun BodyMatchMode.label() = when (this) {
    BodyMatchMode.EXACT -> "Exact"
    BodyMatchMode.CONTAINS -> "Contains"
    BodyMatchMode.REGEX -> "Regex"
}

internal fun HeaderEntryMode.label() = when (this) {
    HeaderEntryMode.KEY_EXISTS -> "Key Exists"
    HeaderEntryMode.VALUE_EXACT -> "Exact"
    HeaderEntryMode.VALUE_CONTAINS -> "Contains"
    HeaderEntryMode.VALUE_REGEX -> "Regex"
}

internal fun UrlMatchMode.isRegex() = this == UrlMatchMode.REGEX
internal fun BodyMatchMode.isRegex() = this == BodyMatchMode.REGEX
internal fun HeaderEntryMode.isRegex() = this == HeaderEntryMode.VALUE_REGEX
internal fun HeaderEntryMode.hasValue() = this != HeaderEntryMode.KEY_EXISTS

internal fun WiretapRule.toUrlMode() = when (urlMatcher) {
    is UrlMatcher.Exact -> UrlMatchMode.EXACT
    is UrlMatcher.Contains -> UrlMatchMode.CONTAINS
    is UrlMatcher.Regex -> UrlMatchMode.REGEX
    null -> null
}

internal fun WiretapRule.toBodyMode() = when (bodyMatcher) {
    is BodyMatcher.Exact -> BodyMatchMode.EXACT
    is BodyMatcher.Contains -> BodyMatchMode.CONTAINS
    is BodyMatcher.Regex -> BodyMatchMode.REGEX
    null -> null
}

internal fun HeaderMatcher.toEntry() = when (this) {
    is HeaderMatcher.KeyExists -> HeaderEntry(key = key, mode = HeaderEntryMode.KEY_EXISTS)
    is HeaderMatcher.ValueExact -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.VALUE_EXACT)
    is HeaderMatcher.ValueContains -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.VALUE_CONTAINS)
    is HeaderMatcher.ValueRegex -> HeaderEntry(key = key, value = pattern, mode = HeaderEntryMode.VALUE_REGEX)
}

internal fun HeaderEntry.toDomain(): HeaderMatcher? {
    if (key.isBlank()) return null
    return when (mode) {
        HeaderEntryMode.KEY_EXISTS -> HeaderMatcher.KeyExists(key.trim())
        HeaderEntryMode.VALUE_EXACT -> HeaderMatcher.ValueExact(key.trim(), value.trim())
        HeaderEntryMode.VALUE_CONTAINS -> HeaderMatcher.ValueContains(key.trim(), value.trim())
        HeaderEntryMode.VALUE_REGEX -> HeaderMatcher.ValueRegex(key.trim(), value.trim())
    }
}

internal fun testRegex(pattern: String, input: String): RegexTestResult {
    return try {
        RegexTestResult(
            matches = pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(input),
            error = null,
        )
    } catch (e: Exception) {
        RegexTestResult(matches = false, error = "Invalid regex: ${e.message}")
    }
}

internal fun urlPlaceholder(mode: UrlMatchMode) = when (mode) {
    UrlMatchMode.EXACT -> "https://api.example.com/users/123"
    UrlMatchMode.CONTAINS -> "/users/"
    UrlMatchMode.REGEX -> "api\\.example\\.com/users/\\d+"
}

internal fun bodyPlaceholder(mode: BodyMatchMode) = when (mode) {
    BodyMatchMode.EXACT -> "{\"status\": \"error\"}"
    BodyMatchMode.CONTAINS -> "\"error\""
    BodyMatchMode.REGEX -> "\"id\":\\s*\\d+"
}

internal fun headerValuePlaceholder(mode: HeaderEntryMode) = when (mode) {
    HeaderEntryMode.VALUE_EXACT -> "Bearer token123"
    HeaderEntryMode.VALUE_CONTAINS -> "Bearer"
    HeaderEntryMode.VALUE_REGEX -> "Bearer\\s+\\S+"
    else -> ""
}
