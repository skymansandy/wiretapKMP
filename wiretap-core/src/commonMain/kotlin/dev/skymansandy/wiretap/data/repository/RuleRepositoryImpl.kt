package dev.skymansandy.wiretap.data.repository

import dev.skymansandy.wiretap.data.db.dao.RuleDao
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.model.BodyMatcher
import dev.skymansandy.wiretap.domain.model.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.UrlMatcher
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow

class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {

    override fun addRule(rule: WiretapRule) = ruleDao.insert(rule)
    override fun updateRule(rule: WiretapRule) = ruleDao.update(rule)
    override fun getAll(): Flow<List<WiretapRule>> = ruleDao.getAll()
    override fun search(query: String): Flow<List<WiretapRule>> = ruleDao.search(query)
    override fun getById(id: Long): WiretapRule? = ruleDao.getById(id)
    override fun getEnabledRules(): List<WiretapRule> = ruleDao.getEnabledRules()
    override fun setEnabled(id: Long, enabled: Boolean) = ruleDao.updateEnabled(id, enabled)
    override fun deleteById(id: Long) = ruleDao.deleteById(id)
    override fun deleteAll() = ruleDao.deleteAll()

    override fun findMatchingRule(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
    ): WiretapRule? {
        return ruleDao.getEnabledRules().firstOrNull { rule ->
            matchesMethod(method, rule.method) && matchesAllCriteria(rule, url, headers, body)
        }
    }

    override fun findConflictingRules(rule: WiretapRule): List<WiretapRule> {
        return ruleDao.getEnabledRules().filter { existing ->
            existing.id != rule.id && rulesOverlap(existing, rule)
        }
    }

    /**
     * Two rules overlap if a request could match both. We check if either rule's
     * matchers are a subset/overlap of the other's on every configured axis.
     */
    private fun rulesOverlap(a: WiretapRule, b: WiretapRule): Boolean {
        // Methods must be compatible
        if (!methodsOverlap(a.method, b.method)) return false
        // URL matchers must be compatible (if both present)
        if (!urlMatchersOverlap(a.urlMatcher, b.urlMatcher)) return false
        // If both rules have body matchers with the same pattern type, check overlap
        if (!bodyMatchersOverlap(a.bodyMatcher, b.bodyMatcher)) return false
        return true
    }

    private fun methodsOverlap(a: String, b: String): Boolean =
        a == "*" || b == "*" || a.equals(b, ignoreCase = true)

    private fun urlMatchersOverlap(a: UrlMatcher?, b: UrlMatcher?): Boolean {
        // If either has no URL matcher, it matches all URLs → overlap possible
        if (a == null || b == null) return true
        // For exact/contains patterns: check if one could match the other's pattern
        return when {
            a is UrlMatcher.Exact && b is UrlMatcher.Exact ->
                a.pattern.equals(b.pattern, ignoreCase = true)
            a is UrlMatcher.Contains && b is UrlMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true) ||
                    b.pattern.contains(a.pattern, ignoreCase = true)
            a is UrlMatcher.Exact && b is UrlMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true)
            a is UrlMatcher.Contains && b is UrlMatcher.Exact ->
                b.pattern.contains(a.pattern, ignoreCase = true)
            // Regex vs anything: conservatively assume overlap
            else -> true
        }
    }

    private fun bodyMatchersOverlap(a: BodyMatcher?, b: BodyMatcher?): Boolean {
        if (a == null || b == null) return true
        return when {
            a is BodyMatcher.Exact && b is BodyMatcher.Exact ->
                a.pattern.equals(b.pattern, ignoreCase = true)
            a is BodyMatcher.Contains && b is BodyMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true) ||
                    b.pattern.contains(a.pattern, ignoreCase = true)
            a is BodyMatcher.Exact && b is BodyMatcher.Contains ->
                a.pattern.contains(b.pattern, ignoreCase = true)
            a is BodyMatcher.Contains && b is BodyMatcher.Exact ->
                b.pattern.contains(a.pattern, ignoreCase = true)
            else -> true
        }
    }

    private fun matchesMethod(requestMethod: String, ruleMethod: String): Boolean =
        ruleMethod == "*" || ruleMethod.equals(requestMethod, ignoreCase = true)

    private fun matchesAllCriteria(
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

    private fun matchesUrl(matcher: UrlMatcher, url: String): Boolean = when (matcher) {
        is UrlMatcher.Exact -> url.equals(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Contains -> url.contains(matcher.pattern, ignoreCase = true)
        is UrlMatcher.Regex -> runCatching {
            matcher.pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(url)
        }.getOrDefault(false)
    }

    private fun matchesHeader(matcher: HeaderMatcher, headers: Map<String, String>): Boolean {
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

    private fun matchesBody(matcher: BodyMatcher, body: String?): Boolean = when (matcher) {
        is BodyMatcher.Exact -> body?.equals(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Contains -> body?.contains(matcher.pattern, ignoreCase = true) == true
        is BodyMatcher.Regex -> runCatching {
            body?.let { matcher.pattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(it) } == true
        }.getOrDefault(false)
    }

    private fun Map<String, String>.headerValue(key: String): String? =
        entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value
}
