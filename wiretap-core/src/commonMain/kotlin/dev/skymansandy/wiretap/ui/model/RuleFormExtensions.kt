/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.model

import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher

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
        RegexTestResult(matches = false, error = e.message)
    }
}

internal val URL_SECTION_INFO = """
    |Match requests whose URL meets the selected condition.
    |
    |• Exact — URL must match the full value exactly
    |• Contains — URL must include the given substring
    |• Regex — URL must match the regular expression pattern
""".trimMargin()

internal val HEADERS_SECTION_INFO = """
    |Match requests that have specific HTTP headers.
    |
    |• Key Exists — matches if the header key is present, regardless of value
    |• Exact — header value must match exactly
    |• Contains — header value must include the given substring
    |• Regex — header value must match the regular expression pattern
""".trimMargin()

internal val BODY_SECTION_INFO = """
    |Match requests whose body content meets the selected condition.
    |
    |• Exact — body must match the full value exactly
    |• Contains — body must include the given substring
    |• Regex — body must match the regular expression pattern
""".trimMargin()

internal fun urlFieldLabel(mode: UrlMatchMode) = when (mode) {
    UrlMatchMode.Exact -> "URL is"
    UrlMatchMode.Contains -> "URL contains"
    UrlMatchMode.Regex -> "URL looks like"
}

internal fun bodyFieldLabel(mode: BodyMatchMode) = when (mode) {
    BodyMatchMode.Exact -> "Body is"
    BodyMatchMode.Contains -> "Body contains"
    BodyMatchMode.Regex -> "Body looks like"
}

internal fun urlWarning(
    mode: UrlMatchMode?,
    pattern: String,
): String? = when {
    mode == null -> null
    pattern.isBlank() -> when (mode) {
        UrlMatchMode.Exact -> "Enter the exact URL to match"
        UrlMatchMode.Contains -> "Enter a substring the URL should contain"
        UrlMatchMode.Regex -> "Enter a regex pattern to match the URL"
    }
    else -> null
}

internal fun bodyWarning(
    mode: BodyMatchMode?,
    pattern: String,
): String? = when {
    mode == null -> null
    pattern.isBlank() -> when (mode) {
        BodyMatchMode.Exact -> "Enter the exact body to match"
        BodyMatchMode.Contains -> "Enter a substring the body should contain"
        BodyMatchMode.Regex -> "Enter a regex pattern to match the body"
    }
    else -> null
}

internal fun headersWarning(entries: List<HeaderEntry>): String? {
    if (entries.isEmpty()) return null
    for (entry in entries) {
        if (entry.key.isBlank()) return "Header key is missing"
        if (entry.mode.hasValue() && entry.value.isBlank()) {
            return when (entry.mode) {
                HeaderEntryMode.ValueExact ->
                    "Enter the exact header value for \"${entry.key}\""
                HeaderEntryMode.ValueContains ->
                    "Enter a substring for header \"${entry.key}\""
                HeaderEntryMode.ValueRegex ->
                    "Enter a regex pattern for header \"${entry.key}\""
                else -> null
            }
        }
    }
    return null
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

internal val ThrottleProfile.labelText: String
    get() = label

internal val ThrottleProfile.speedText: String
    get() = when (this) {
        ThrottleProfile.Gprs -> "~50 kbps"
        ThrottleProfile.Edge -> "~200 kbps"
        ThrottleProfile.Slow3g -> "~400 kbps"
        ThrottleProfile.Fast3g -> "~2 Mbps"
        ThrottleProfile.Slow4g -> "~5 Mbps"
        ThrottleProfile.Lte -> "~20 Mbps"
        ThrottleProfile.SlowWifi -> "~1 Mbps"
    }
