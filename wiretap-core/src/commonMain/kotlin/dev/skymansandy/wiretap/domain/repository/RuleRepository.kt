package dev.skymansandy.wiretap.domain.repository

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    fun addRule(rule: WiretapRule)
    fun updateRule(rule: WiretapRule)
    fun getAll(): Flow<List<WiretapRule>>
    fun search(query: String): Flow<List<WiretapRule>>
    fun getEnabledRules(): List<WiretapRule>
    fun findMatchingRule(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): WiretapRule?
    fun setEnabled(id: Long, enabled: Boolean)
    fun deleteById(id: Long)
    fun deleteAll()
}
