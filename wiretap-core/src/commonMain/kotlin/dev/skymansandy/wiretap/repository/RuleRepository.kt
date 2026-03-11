package dev.skymansandy.wiretap.repository

import dev.skymansandy.wiretap.model.WiretapRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun addRule(rule: WiretapRule)
    fun getAll(): Flow<List<WiretapRule>>
    fun getEnabledRules(): List<WiretapRule>
    fun findMatchingRule(url: String, method: String): WiretapRule?
    fun setEnabled(id: Long, enabled: Boolean)
    fun deleteById(id: Long)
    fun deleteAll()
}
