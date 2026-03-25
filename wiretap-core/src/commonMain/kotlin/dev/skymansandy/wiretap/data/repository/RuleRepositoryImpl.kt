package dev.skymansandy.wiretap.data.repository

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.data.db.room.dao.RulesDao
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RuleRepositoryImpl(
    private val rulesDao: RulesDao,
) : RuleRepository {

    override suspend fun addRule(rule: WiretapRule) {
        rulesDao.insert(rule.toRoomEntity())
    }

    override suspend fun updateRule(rule: WiretapRule) {
        rulesDao.update(
            method = rule.method,
            urlMatcherType = rule.urlMatcher?.type?.name,
            urlPattern = rule.urlMatcher?.pattern,
            headerMatchers = rule.headerMatchers.takeIf { it.isNotEmpty() }
                ?.let { HeaderMatcherSerializer.serialize(it) },
            bodyMatcherType = rule.bodyMatcher?.type?.name,
            bodyPattern = rule.bodyMatcher?.pattern,
            action = rule.action.name,
            mockResponseCode = (rule.action as? RuleAction.Mock)?.responseCode?.toLong(),
            mockResponseBody = (rule.action as? RuleAction.Mock)?.responseBody,
            mockResponseHeaders = (rule.action as? RuleAction.Mock)?.responseHeaders
                ?.let { HeadersSerializerUtil.serialize(it) },
            throttleDelayMs = when (val action = rule.action) {
                is RuleAction.Mock -> action.throttleDelayMs
                is RuleAction.Throttle -> action.delayMs
            },
            throttleDelayMaxMs = when (val action = rule.action) {
                is RuleAction.Mock -> action.throttleDelayMaxMs
                is RuleAction.Throttle -> action.delayMaxMs
            },
            enabled = if (rule.enabled) 1L else 0L,
            id = rule.id,
        )
    }

    override fun getAll(): Flow<List<WiretapRule>> =
        rulesDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun search(query: String): Flow<List<WiretapRule>> =
        rulesDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): WiretapRule? =
        rulesDao.getById(id)?.toDomain()

    override suspend fun getEnabledRules(): List<WiretapRule> =
        rulesDao.getEnabledRules().map { it.toDomain() }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        rulesDao.updateEnabled(
            enabled = if (enabled) 1L else 0L,
            id = id,
        )
    }

    override suspend fun deleteById(id: Long) = rulesDao.deleteById(id)

    override suspend fun deleteAll() = rulesDao.deleteAll()
}

private fun WiretapRule.toRoomEntity(): RuleEntity {
    return RuleEntity(
        id = id,
        method = method,
        urlMatcherType = urlMatcher?.type?.name,
        urlPattern = urlMatcher?.pattern,
        headerMatchers = headerMatchers.takeIf { it.isNotEmpty() }
            ?.let { HeaderMatcherSerializer.serialize(it) },
        bodyMatcherType = bodyMatcher?.type?.name,
        bodyPattern = bodyMatcher?.pattern,
        action = action.name,
        mockResponseCode = (action as? RuleAction.Mock)?.responseCode?.toLong(),
        mockResponseBody = (action as? RuleAction.Mock)?.responseBody,
        mockResponseHeaders = (action as? RuleAction.Mock)?.responseHeaders
            ?.let { HeadersSerializerUtil.serialize(it) },
        throttleDelayMs = when (action) {
            is RuleAction.Mock -> action.throttleDelayMs
            is RuleAction.Throttle -> action.delayMs
        },
        throttleDelayMaxMs = when (action) {
            is RuleAction.Mock -> action.throttleDelayMaxMs
            is RuleAction.Throttle -> action.delayMaxMs
        },
        enabled = if (enabled) 1L else 0L,
        createdAt = createdAt,
    )
}
