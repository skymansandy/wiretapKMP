package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import kotlinx.coroutines.flow.Flow

internal interface RuleDao {

    fun insert(rule: WiretapRule)
    fun getAll(): Flow<List<WiretapRule>>
    fun getById(id: Long): WiretapRule?
    fun getEnabledRules(): List<WiretapRule>
    fun update(rule: WiretapRule)
    fun search(query: String): Flow<List<WiretapRule>>
    fun updateEnabled(id: Long, enabled: Boolean)
    fun deleteById(id: Long)
    fun deleteAll()
}
