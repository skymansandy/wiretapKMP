package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher

internal object RuleMatcher {

    fun matchesMethod(requestMethod: String, ruleMethod: String): Boolean =
        ruleMethod == "*" || ruleMethod.equals(requestMethod, ignoreCase = true)

    fun matchesAllCriteria(
        rule: WiretapRule,
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): Boolean {
        // A rule must have at least one criterion, otherwise it matches nothing.
        if (rule.urlMatcher == null && rule.headerMatchers.isEmpty() && rule.bodyMatcher == null) return false

        // Each configured criterion must match (AND logic across url/headers/body).
        rule.urlMatcher?.let { if (!matchesUrl(it, url)) return false }
        if (rule.headerMatchers.isNotEmpty() && !rule.headerMatchers.all { matchesHeader(it, headers) }) return false
        rule.bodyMatcher?.let { if (!matchesBody(it, body)) return false }

        return true
    }

    fun matchesUrl(matcher: UrlMatcher, url: String): Boolean = when (matcher) {
        is UrlMatcher.Exact -> url.equals(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Contains -> url.contains(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Regex -> runCatching {
            matcher.pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(url)
        }.getOrDefault(false)
    }

    fun matchesHeader(matcher: HeaderMatcher, headers: Map<String, String>): Boolean {
        return when (matcher) {
            is HeaderMatcher.KeyExists ->
                headers.keys.any { it.equals(matcher.key, ignoreCase = true) }

            is HeaderMatcher.ValueExact -> {
                val value = headers.headerValue(matcher.key)
                value?.equals(matcher.value, ignoreCase = true) == true
            }

            is HeaderMatcher.ValueContains -> {
                val value = headers.headerValue(matcher.key)
                value?.contains(matcher.value, ignoreCase = true) == true
            }

            is HeaderMatcher.ValueRegex -> {
                val value = headers.headerValue(matcher.key) ?: return false
                runCatching {
                    matcher.pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(value)
                }.getOrDefault(false)
            }
        }
    }

    fun matchesBody(matcher: BodyMatcher, body: String?): Boolean = when (matcher) {
        is BodyMatcher.Exact -> body?.equals(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Contains -> body?.contains(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Regex -> runCatching {
            body?.let {
                matcher.pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(it)
            } == true
        }.getOrDefault(false)
    }

    fun methodsOverlap(a: String, b: String): Boolean =
        a == "*" || b == "*" || a.equals(b, ignoreCase = true)

    fun urlMatchersOverlap(a: UrlMatcher?, b: UrlMatcher?): Boolean {
        // If either has no URL matcher, it matches all URLs → overlap possible
        if (a == null || b == null) return true

        return when (a) {
            is UrlMatcher.Exact if b is UrlMatcher.Exact ->
                a.pattern.equals(b.pattern, ignoreCase = true)

            // Any two Contains patterns can co-exist in a single URL, so always overlap
            is UrlMatcher.Contains if b is UrlMatcher.Contains -> true

            is UrlMatcher.Exact if b is UrlMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true)

            is UrlMatcher.Contains if b is UrlMatcher.Exact ->
                b.pattern.contains(a.pattern, ignoreCase = true)
            // Regex vs anything: conservatively assume overlap
            else -> true
        }
    }

    fun headerMatchersOverlap(a: List<HeaderMatcher>, b: List<HeaderMatcher>): Boolean {
        // If either side has no header matchers it matches all headers → overlap possible
        if (a.isEmpty() || b.isEmpty()) return true

        // Group matchers by key (case-insensitive).
        // For each key present in BOTH rules, check whether the matchers are compatible.
        val aByKey = a.groupBy { it.key.lowercase() }
        val bByKey = b.groupBy { it.key.lowercase() }

        val sharedKeys = aByKey.keys.intersect(bByKey.keys)
        // Keys only in one side don't prevent overlap (a request can have any headers).
        // But shared keys must have compatible matchers.
        return sharedKeys.all { key ->
            val aMatchers = aByKey.getValue(key)
            val bMatchers = bByKey.getValue(key)
            aMatchers.all { am ->
                bMatchers.all { bm ->
                    singleHeaderMatchersOverlap(am, bm)
                }
            }
        }
    }

    private fun singleHeaderMatchersOverlap(a: HeaderMatcher, b: HeaderMatcher): Boolean {
        // KeyExists overlaps with everything (it only checks presence)
        if (a is HeaderMatcher.KeyExists || b is HeaderMatcher.KeyExists) return true

        return when {
            a is HeaderMatcher.ValueExact && b is HeaderMatcher.ValueExact ->
                a.value.equals(b.value, ignoreCase = true)

            a is HeaderMatcher.ValueContains && b is HeaderMatcher.ValueContains ->
                a.value.contains(b.value, ignoreCase = true) ||
                    b.value.contains(a.value, ignoreCase = true)

            a is HeaderMatcher.ValueExact && b is HeaderMatcher.ValueContains ->
                a.value.contains(b.value, ignoreCase = true)

            a is HeaderMatcher.ValueContains && b is HeaderMatcher.ValueExact ->
                b.value.contains(a.value, ignoreCase = true)

            // Regex vs anything: conservatively assume overlap
            else -> true
        }
    }

    fun bodyMatchersOverlap(a: BodyMatcher?, b: BodyMatcher?): Boolean {
        if (a == null || b == null) return true
        return when {
            a is BodyMatcher.Exact && b is BodyMatcher.Exact ->
                a.pattern.equals(b.pattern, ignoreCase = true)

            // Any two Contains patterns can co-exist in a single body, so always overlap
            a is BodyMatcher.Contains && b is BodyMatcher.Contains -> true

            a is BodyMatcher.Exact && b is BodyMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true)

            a is BodyMatcher.Contains && b is BodyMatcher.Exact ->
                b.pattern.contains(a.pattern, ignoreCase = true)

            else -> true
        }
    }

    /**
     * Two rules overlap if a request could match both.
     */
    fun rulesOverlap(a: WiretapRule, b: WiretapRule): Boolean {
        if (!methodsOverlap(a.method, b.method)) return false
        if (!urlMatchersOverlap(a.urlMatcher, b.urlMatcher)) return false
        if (!headerMatchersOverlap(a.headerMatchers, b.headerMatchers)) return false
        if (!bodyMatchersOverlap(a.bodyMatcher, b.bodyMatcher)) return false
        return true
    }

    private fun Map<String, String>.headerValue(key: String): String? =
        entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
}
