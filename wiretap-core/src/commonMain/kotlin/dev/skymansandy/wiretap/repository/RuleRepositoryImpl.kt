package dev.skymansandy.wiretap.repository

import dev.skymansandy.wiretap.dao.RuleDao
import dev.skymansandy.wiretap.model.WiretapRule
import kotlinx.coroutines.flow.Flow

class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {

    override fun addRule(rule: WiretapRule) {
        ruleDao.insert(rule)
    }

    override fun getAll(): Flow<List<WiretapRule>> {
        return ruleDao.getAll()
    }

    override fun getEnabledRules(): List<WiretapRule> {
        return ruleDao.getEnabledRules()
    }

    override fun findMatchingRule(url: String, method: String): WiretapRule? {
        val rules = ruleDao.getEnabledRules()
        return rules.firstOrNull { rule ->
            matchesUrl(url, rule.urlPattern) &&
                (rule.method == "*" || rule.method.equals(method, ignoreCase = true))
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

    private fun matchesUrl(url: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .toRegex(RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(url)
    }
}
