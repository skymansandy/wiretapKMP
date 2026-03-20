package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import kotlinx.coroutines.flow.Flow

internal interface RuleDao {

    suspend fun insert(rule: WiretapRule)

    fun getAll(): Flow<List<WiretapRule>>

    suspend fun getById(id: Long): WiretapRule?

    suspend fun getEnabledRules(): List<WiretapRule>

    suspend fun update(rule: WiretapRule)

    fun search(query: String): Flow<List<WiretapRule>>

    suspend fun updateEnabled(id: Long, enabled: Boolean)

    suspend fun deleteById(id: Long)

    suspend fun deleteAll()
}
