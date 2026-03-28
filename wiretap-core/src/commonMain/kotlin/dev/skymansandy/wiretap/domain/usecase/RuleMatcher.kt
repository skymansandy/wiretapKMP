package dev.skymansandy.wiretap.domain.usecase

import dev.skymansandy.wiretap.domain.model.MatcherType
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher

internal object RuleMatcher {

    fun matchesMethod(requestMethod: String, ruleMethod: String): Boolean =
        ruleMethod == "*" || ruleMethod.equals(requestMethod, ignoreCase = true)

    fun matchesAllCriteria(
        rule: WiretapRule,
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): Boolean {
        if (rule.urlMatcher == null && rule.headerMatchers.isEmpty() && rule.bodyMatcher == null) return false

        rule.urlMatcher?.let { if (!matchesUrl(it, url)) return false }
        if (rule.headerMatchers.any { !matchesHeader(it, headers) }) return false
        rule.bodyMatcher?.let { if (!matchesBody(it, body)) return false }

        return true
    }

    fun matchesUrl(matcher: UrlMatcher, url: String): Boolean = when (matcher) {
        is UrlMatcher.Exact -> url.equals(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Contains -> url.contains(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Regex -> matchesRegex(matcher.pattern, url)
    }

    fun matchesHeader(matcher: HeaderMatcher, headers: Map<String, String>): Boolean {
        val value by lazy { headers.headerValue(matcher.key) }
        return when (matcher) {
            is HeaderMatcher.KeyExists -> headers.keys.any { it.equals(matcher.key, ignoreCase = true) }
            is HeaderMatcher.ValueExact -> value?.equals(matcher.value, ignoreCase = true) == true
            is HeaderMatcher.ValueContains -> value?.contains(matcher.value, ignoreCase = true) == true
            is HeaderMatcher.ValueRegex -> value?.let { matchesRegex(matcher.pattern, it) } == true
        }
    }

    fun matchesBody(matcher: BodyMatcher, body: String?): Boolean = when (matcher) {
        is BodyMatcher.Exact -> body?.equals(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Contains -> body?.contains(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Regex -> body?.let { matchesRegex(matcher.pattern, it) } == true
    }

    fun methodsOverlap(a: String, b: String): Boolean =
        a == "*" || b == "*" || a.equals(b, ignoreCase = true)

    fun urlMatchersOverlap(a: UrlMatcher?, b: UrlMatcher?): Boolean {
        if (a == null || b == null) return true
        return patternsOverlap(a.type, a.pattern, b.type, b.pattern)
    }

    fun headerMatchersOverlap(a: List<HeaderMatcher>, b: List<HeaderMatcher>): Boolean {
        if (a.isEmpty() || b.isEmpty()) return true

        val aByKey = a.groupBy { it.key.lowercase() }
        val bByKey = b.groupBy { it.key.lowercase() }

        return aByKey.keys.intersect(bByKey.keys).all { key ->
            val aMatchers = aByKey.getValue(key)
            val bMatchers = bByKey.getValue(key)
            aMatchers.all { am -> bMatchers.all { bm -> singleHeaderMatchersOverlap(am, bm) } }
        }
    }

    fun bodyMatchersOverlap(a: BodyMatcher?, b: BodyMatcher?): Boolean {
        if (a == null || b == null) return true
        return patternsOverlap(a.type, a.pattern, b.type, b.pattern)
    }

    fun rulesOverlap(a: WiretapRule, b: WiretapRule): Boolean =
        methodsOverlap(a.method, b.method) &&
            urlMatchersOverlap(a.urlMatcher, b.urlMatcher) &&
            headerMatchersOverlap(a.headerMatchers, b.headerMatchers) &&
            bodyMatchersOverlap(a.bodyMatcher, b.bodyMatcher)

    // -- Private helpers --

    private fun matchesRegex(pattern: String, input: String): Boolean = runCatching {
        pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(input)
    }.getOrDefault(false)

    private fun patternsOverlap(
        aType: MatcherType,
        aPattern: String,
        bType: MatcherType,
        bPattern: String,
    ): Boolean = when {
        aType == MatcherType.Regex || bType == MatcherType.Regex -> true

        aType == MatcherType.Exact && bType == MatcherType.Exact ->
            aPattern.equals(bPattern, ignoreCase = true)

        aType == MatcherType.Contains && bType == MatcherType.Contains -> true

        aType == MatcherType.Exact && bType == MatcherType.Contains ->
            aPattern.contains(bPattern, ignoreCase = true)

        aType == MatcherType.Contains && bType == MatcherType.Exact ->
            bPattern.contains(aPattern, ignoreCase = true)

        else -> true
    }

    private fun singleHeaderMatchersOverlap(a: HeaderMatcher, b: HeaderMatcher): Boolean {
        if (a is HeaderMatcher.KeyExists || b is HeaderMatcher.KeyExists) return true
        return patternsOverlap(a.matcherType(), a.valuePattern(), b.matcherType(), b.valuePattern())
    }

    private fun HeaderMatcher.matcherType(): MatcherType = when (this) {
        is HeaderMatcher.KeyExists -> MatcherType.Exact
        is HeaderMatcher.ValueExact -> MatcherType.Exact
        is HeaderMatcher.ValueContains -> MatcherType.Contains
        is HeaderMatcher.ValueRegex -> MatcherType.Regex
    }

    private fun HeaderMatcher.valuePattern(): String = when (this) {
        is HeaderMatcher.KeyExists -> key
        is HeaderMatcher.ValueExact -> value
        is HeaderMatcher.ValueContains -> value
        is HeaderMatcher.ValueRegex -> pattern
    }

    private fun Map<String, String>.headerValue(key: String): String? =
        entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
}
