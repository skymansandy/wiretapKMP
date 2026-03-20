package dev.skymansandy.wiretap.data.repository

import dev.skymansandy.wiretap.data.db.dao.RuleDao
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow

internal class RuleRepositoryImpl(
    private val ruleDao: RuleDao,
) : RuleRepository {

    override suspend fun addRule(rule: WiretapRule) = ruleDao.insert(rule)

    override suspend fun updateRule(rule: WiretapRule) = ruleDao.update(rule)

    override fun getAll(): Flow<List<WiretapRule>> = ruleDao.getAll()

    override fun search(query: String): Flow<List<WiretapRule>> = ruleDao.search(query)

    override suspend fun getById(id: Long): WiretapRule? = ruleDao.getById(id)

    override suspend fun getEnabledRules(): List<WiretapRule> = ruleDao.getEnabledRules()

    override suspend fun setEnabled(id: Long, enabled: Boolean) = ruleDao.updateEnabled(id, enabled)

    override suspend fun deleteById(id: Long) = ruleDao.deleteById(id)

    override suspend fun deleteAll() = ruleDao.deleteAll()
}
