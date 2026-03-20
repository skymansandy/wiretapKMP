package dev.skymansandy.wiretap.ui.model

import androidx.compose.runtime.Composable
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.match_contains
import dev.skymansandy.wiretap.resources.match_exact
import dev.skymansandy.wiretap.resources.match_key_exists
import dev.skymansandy.wiretap.resources.match_regex
import dev.skymansandy.wiretap.resources.profile_3g
import dev.skymansandy.wiretap.resources.profile_edge
import dev.skymansandy.wiretap.resources.profile_gprs
import dev.skymansandy.wiretap.resources.profile_lte
import dev.skymansandy.wiretap.resources.profile_slow_3g
import dev.skymansandy.wiretap.resources.profile_slow_4g
import dev.skymansandy.wiretap.resources.profile_slow_wifi
import dev.skymansandy.wiretap.resources.speed_3g
import dev.skymansandy.wiretap.resources.speed_edge
import dev.skymansandy.wiretap.resources.speed_gprs
import dev.skymansandy.wiretap.resources.speed_lte
import dev.skymansandy.wiretap.resources.speed_slow_3g
import dev.skymansandy.wiretap.resources.speed_slow_4g
import dev.skymansandy.wiretap.resources.speed_slow_wifi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UrlMatchMode.label() = when (this) {
    UrlMatchMode.Exact -> stringResource(Res.string.match_exact)
    UrlMatchMode.Contains -> stringResource(Res.string.match_contains)
    UrlMatchMode.Regex -> stringResource(Res.string.match_regex)
}

@Composable
internal fun BodyMatchMode.label() = when (this) {
    BodyMatchMode.Exact -> stringResource(Res.string.match_exact)
    BodyMatchMode.Contains -> stringResource(Res.string.match_contains)
    BodyMatchMode.Regex -> stringResource(Res.string.match_regex)
}

@Composable
internal fun HeaderEntryMode.label() = when (this) {
    HeaderEntryMode.KeyExists -> stringResource(Res.string.match_key_exists)
    HeaderEntryMode.ValueExact -> stringResource(Res.string.match_exact)
    HeaderEntryMode.ValueContains -> stringResource(Res.string.match_contains)
    HeaderEntryMode.ValueRegex -> stringResource(Res.string.match_regex)
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

internal val ThrottleProfile.labelRes: StringResource
    get() = when (this) {
        ThrottleProfile.Gprs -> Res.string.profile_gprs
        ThrottleProfile.Edge -> Res.string.profile_edge
        ThrottleProfile.Slow3g -> Res.string.profile_slow_3g
        ThrottleProfile.Fast3g -> Res.string.profile_3g
        ThrottleProfile.Slow4g -> Res.string.profile_slow_4g
        ThrottleProfile.Lte -> Res.string.profile_lte
        ThrottleProfile.SlowWifi -> Res.string.profile_slow_wifi
    }

internal val ThrottleProfile.speedRes: StringResource
    get() = when (this) {
        ThrottleProfile.Gprs -> Res.string.speed_gprs
        ThrottleProfile.Edge -> Res.string.speed_edge
        ThrottleProfile.Slow3g -> Res.string.speed_slow_3g
        ThrottleProfile.Fast3g -> Res.string.speed_3g
        ThrottleProfile.Slow4g -> Res.string.speed_slow_4g
        ThrottleProfile.Lte -> Res.string.speed_lte
        ThrottleProfile.SlowWifi -> Res.string.speed_slow_wifi
    }
