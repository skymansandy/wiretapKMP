package dev.skymansandy.wiretap.ui.model

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher

internal fun UrlMatchMode.label() = when (this) {
    UrlMatchMode.Exact -> "Exact"
    UrlMatchMode.Contains -> "Contains"
    UrlMatchMode.Regex -> "Regex"
}

internal fun BodyMatchMode.label() = when (this) {
    BodyMatchMode.Exact -> "Exact"
    BodyMatchMode.Contains -> "Contains"
    BodyMatchMode.Regex -> "Regex"
}

internal fun HeaderEntryMode.label() = when (this) {
    HeaderEntryMode.KeyExists -> "Key Exists"
    HeaderEntryMode.ValueExact -> "Exact"
    HeaderEntryMode.ValueContains -> "Contains"
    HeaderEntryMode.ValueRegex -> "Regex"
}

internal fun UrlMatchMode.isRegex() = this == UrlMatchMode.Regex

internal fun BodyMatchMode.isRegex() = this == BodyMatchMode.Regex

internal fun HeaderEntryMode.isRegex() = this == HeaderEntryMode.ValueRegex

internal fun HeaderEntryMode.hasValue() = this != HeaderEntryMode.KeyExists

internal fun WiretapRule.toUrlMode() = when (urlMatcher) {
    is UrlMatcher.Exact -> UrlMatchMode.Exact
    is UrlMatcher.Contains -> UrlMatchMode.Contains
    is UrlMatcher.Regex -> UrlMatchMode.Regex
    null -> null
}

internal fun WiretapRule.toBodyMode() = when (bodyMatcher) {
    is BodyMatcher.Exact -> BodyMatchMode.Exact
    is BodyMatcher.Contains -> BodyMatchMode.Contains
    is BodyMatcher.Regex -> BodyMatchMode.Regex
    null -> null
}

internal fun HeaderMatcher.toEntry() = when (this) {
    is HeaderMatcher.KeyExists -> HeaderEntry(key = key, mode = HeaderEntryMode.KeyExists)
    is HeaderMatcher.ValueExact -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.ValueExact)
    is HeaderMatcher.ValueContains -> HeaderEntry(key = key, value = value, mode = HeaderEntryMode.ValueContains)
    is HeaderMatcher.ValueRegex -> HeaderEntry(key = key, value = pattern, mode = HeaderEntryMode.ValueRegex)
}

internal fun HeaderEntry.toDomain(): HeaderMatcher? {
    if (key.isBlank()) return null
    return when (mode) {
        HeaderEntryMode.KeyExists -> HeaderMatcher.KeyExists(key.trim())
        HeaderEntryMode.ValueExact -> HeaderMatcher.ValueExact(key.trim(), value.trim())
        HeaderEntryMode.ValueContains -> HeaderMatcher.ValueContains(key.trim(), value.trim())
        HeaderEntryMode.ValueRegex -> HeaderMatcher.ValueRegex(key.trim(), value.trim())
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
    UrlMatchMode.Exact -> "https://api.example.com/users/123"
    UrlMatchMode.Contains -> "/users/"
    UrlMatchMode.Regex -> "api\\.example\\.com/users/\\d+"
}

internal fun bodyPlaceholder(mode: BodyMatchMode) = when (mode) {
    BodyMatchMode.Exact -> "{\"status\": \"error\"}"
    BodyMatchMode.Contains -> "\"error\""
    BodyMatchMode.Regex -> "\"id\":\\s*\\d+"
}

internal fun headerValuePlaceholder(mode: HeaderEntryMode) = when (mode) {
    HeaderEntryMode.ValueExact -> "Bearer token123"
    HeaderEntryMode.ValueContains -> "Bearer"
    HeaderEntryMode.ValueRegex -> "Bearer\\s+\\S+"
    else -> ""
}
