package dev.skymansandy.wiretap.domain.repository

import dev.skymansandy.wiretap.domain.model.WiretapRule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {

    fun flowAll(): Flow<List<WiretapRule>>

    fun flowForQuery(query: String): Flow<List<WiretapRule>>

    suspend fun addRule(rule: WiretapRule)

    suspend fun updateRule(rule: WiretapRule)

    suspend fun getById(id: Long): WiretapRule?

    suspend fun getEnabledRules(): List<WiretapRule>

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun deleteById(id: Long)

    suspend fun deleteAll()
}
