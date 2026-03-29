/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import dev.skymansandy.wiretap.data.db.room.dao.RulesDao
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.data.mappers.toRoomEntity
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RuleRepositoryImpl(
    private val rulesDao: RulesDao,
) : RuleRepository {

    override fun flowAll(): Flow<List<WiretapRule>> =
        rulesDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun flowForQuery(query: String): Flow<List<WiretapRule>> =
        rulesDao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun flowById(id: Long): Flow<WiretapRule?> =
        rulesDao.flowById(id).map { it?.toDomain() }

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
            mockResponseCode = when (rule.action) {
                is RuleAction.Mock -> rule.action.responseCode.toLong()
                is RuleAction.MockAndThrottle -> rule.action.responseCode.toLong()
                is RuleAction.Throttle -> null
            },
            mockResponseBody = when (rule.action) {
                is RuleAction.Mock -> rule.action.responseBody
                is RuleAction.MockAndThrottle -> rule.action.responseBody
                is RuleAction.Throttle -> null
            },
            mockResponseHeaders = when (rule.action) {
                is RuleAction.Mock -> rule.action.responseHeaders
                    ?.let { HeadersSerializerUtil.serialize(it) }
                is RuleAction.MockAndThrottle -> rule.action.responseHeaders
                    ?.let { HeadersSerializerUtil.serialize(it) }
                is RuleAction.Throttle -> null
            },
            throttleDelayMs = when (val action = rule.action) {
                is RuleAction.Mock -> null
                is RuleAction.Throttle -> action.delayMs
                is RuleAction.MockAndThrottle -> action.delayMs
            },
            throttleDelayMaxMs = when (val action = rule.action) {
                is RuleAction.Mock -> null
                is RuleAction.Throttle -> action.delayMaxMs
                is RuleAction.MockAndThrottle -> action.delayMaxMs
            },
            enabled = if (rule.enabled) 1L else 0L,
            id = rule.id,
        )
    }

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
