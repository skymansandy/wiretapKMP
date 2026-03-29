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
