package dev.skymansandy.wiretap.domain.repository

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {

    suspend fun addRule(rule: WiretapRule)

    suspend fun updateRule(rule: WiretapRule)

    fun getAll(): Flow<List<WiretapRule>>

    fun search(query: String): Flow<List<WiretapRule>>

    suspend fun getById(id: Long): WiretapRule?

    suspend fun getEnabledRules(): List<WiretapRule>

    suspend fun findMatchingRule(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): WiretapRule?

    suspend fun findConflictingRules(
        rule: WiretapRule,
    ): List<WiretapRule>

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun deleteById(id: Long)

    suspend fun deleteAll()
}
