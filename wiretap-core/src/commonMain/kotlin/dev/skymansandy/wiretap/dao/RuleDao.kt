package dev.skymansandy.wiretap.dao

import dev.skymansandy.wiretap.model.WiretapRule
import kotlinx.coroutines.flow.Flow

interface RuleDao {
    fun insert(rule: WiretapRule)
    fun getAll(): Flow<List<WiretapRule>>
    fun getById(id: Long): WiretapRule?
    fun getEnabledRules(): List<WiretapRule>
    fun updateEnabled(id: Long, enabled: Boolean)
    fun deleteById(id: Long)
    fun deleteAll()
}
