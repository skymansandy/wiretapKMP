package dev.skymansandy.wiretap.data.repository

import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.data.db.room.dao.RuleRoomDao
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RuleRepositoryImpl(
    private val ruleRoomDao: RuleRoomDao,
) : RuleRepository {

    override suspend fun addRule(rule: WiretapRule) {
        ruleRoomDao.insert(rule.toRoomEntity())
    }

    override suspend fun updateRule(rule: WiretapRule) {
        ruleRoomDao.update(
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
        ruleRoomDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun search(query: String): Flow<List<WiretapRule>> =
        ruleRoomDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): WiretapRule? =
        ruleRoomDao.getById(id)?.toDomain()

    override suspend fun getEnabledRules(): List<WiretapRule> =
        ruleRoomDao.getEnabledRules().map { it.toDomain() }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        ruleRoomDao.updateEnabled(
            enabled = if (enabled) 1L else 0L,
            id = id,
        )
    }

    override suspend fun deleteById(id: Long) = ruleRoomDao.deleteById(id)

    override suspend fun deleteAll() = ruleRoomDao.deleteAll()
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
