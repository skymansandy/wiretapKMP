package dev.skymansandy.wiretap.repository

import dev.skymansandy.wiretap.dao.RuleDao
import dev.skymansandy.wiretap.model.MatcherType
import dev.skymansandy.wiretap.model.WiretapRule
import kotlinx.coroutines.flow.Flow

class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {

    override fun addRule(rule: WiretapRule) {
        ruleDao.insert(rule)
    }

    override fun updateRule(rule: WiretapRule) {
        ruleDao.update(rule)
    }

    override fun getAll(): Flow<List<WiretapRule>> {
        return ruleDao.getAll()
    }

    override fun search(query: String): Flow<List<WiretapRule>> {
        return ruleDao.search(query)
    }

    override fun getEnabledRules(): List<WiretapRule> {
        return ruleDao.getEnabledRules()
    }

    override fun findMatchingRule(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
    ): WiretapRule? {
        val rules = ruleDao.getEnabledRules()
        return rules.firstOrNull { rule ->
            matchesMethod(method, rule.method) && matchesPattern(rule, url, headers, body)
        }
    }

    override fun setEnabled(id: Long, enabled: Boolean) {
        ruleDao.updateEnabled(id, enabled)
    }

    override fun deleteById(id: Long) {
        ruleDao.deleteById(id)
    }

    override fun deleteAll() {
        ruleDao.deleteAll()
    }

    private fun matchesMethod(requestMethod: String, ruleMethod: String): Boolean {
        return ruleMethod == "*" || ruleMethod.equals(requestMethod, ignoreCase = true)
    }

    private fun matchesPattern(
        rule: WiretapRule,
        url: String,
        headers: Map<String, String>,
        body: String?,
    ): Boolean {
        return when (rule.matcherType) {
            MatcherType.URL_EXACT -> url.equals(rule.urlPattern, ignoreCase = true)

            MatcherType.URL_REGEX -> {
                try {
                    rule.urlPattern.toRegex(RegexOption.IGNORE_CASE).containsMatchIn(url)
                } catch (_: Exception) {
                    false
                }
            }

            MatcherType.HEADER_CONTAINS -> {
                val pattern = rule.urlPattern
                headers.any { (key, value) ->
                    "$key: $value".contains(pattern, ignoreCase = true) ||
                        key.contains(pattern, ignoreCase = true)
                }
            }

            MatcherType.BODY_CONTAINS -> {
                body?.contains(rule.urlPattern, ignoreCase = true) == true
            }
        }
    }
}
