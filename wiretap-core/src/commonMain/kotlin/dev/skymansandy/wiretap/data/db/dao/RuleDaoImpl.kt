package dev.skymansandy.wiretap.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.helper.util.HeaderMatcherSerializer
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class RuleDaoImpl(
    private val database: WiretapDatabase,
) : RuleDao {

    private val queries get() = database.wiretapQueries

    override suspend fun insert(rule: WiretapRule) {
        withContext(Dispatchers.IO) {
            queries.insertRule(
                method = rule.method,
                url_matcher_type = rule.urlMatcher?.type?.name,
                url_pattern = rule.urlMatcher?.pattern,
                header_matchers = rule.headerMatchers.takeIf { it.isNotEmpty() }
                    ?.let { HeaderMatcherSerializer.serialize(it) },
                body_matcher_type = rule.bodyMatcher?.type?.name,
                body_pattern = rule.bodyMatcher?.pattern,
                action = rule.action.name,
                mock_response_code = (rule.action as? RuleAction.Mock)?.responseCode?.toLong(),
                mock_response_body = (rule.action as? RuleAction.Mock)?.responseBody,
                mock_response_headers = (rule.action as? RuleAction.Mock)?.responseHeaders
                    ?.let { HeadersSerializerUtil.serialize(it) },
                throttle_delay_ms = when (val action = rule.action) {
                    is RuleAction.Mock -> action.throttleDelayMs
                    is RuleAction.Throttle -> action.delayMs
                },
                throttle_delay_max_ms = when (val action = rule.action) {
                    is RuleAction.Mock -> action.throttleDelayMaxMs
                    is RuleAction.Throttle -> action.delayMaxMs
                },
                enabled = if (rule.enabled) 1L else 0L,
                created_at = rule.createdAt,
            )
        }
    }

    override fun getAll(): Flow<List<WiretapRule>> {
        return queries.getAllRules()
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): WiretapRule? = withContext(Dispatchers.IO) {
        queries.getRuleById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun getEnabledRules(): List<WiretapRule> = withContext(Dispatchers.IO) {
        queries.getEnabledRules().executeAsList().map { it.toDomain() }
    }

    override suspend fun update(rule: WiretapRule) {
        withContext(Dispatchers.IO) {
            queries.updateRule(
                method = rule.method,
                url_matcher_type = rule.urlMatcher?.type?.name,
                url_pattern = rule.urlMatcher?.pattern,
                header_matchers = rule.headerMatchers.takeIf { it.isNotEmpty() }
                    ?.let { HeaderMatcherSerializer.serialize(it) },
                body_matcher_type = rule.bodyMatcher?.type?.name,
                body_pattern = rule.bodyMatcher?.pattern,
                action = rule.action.name,
                mock_response_code = (rule.action as? RuleAction.Mock)?.responseCode?.toLong(),
                mock_response_body = (rule.action as? RuleAction.Mock)?.responseBody,
                mock_response_headers = (rule.action as? RuleAction.Mock)?.responseHeaders
                    ?.let { HeadersSerializerUtil.serialize(it) },
                throttle_delay_ms = when (val action = rule.action) {
                    is RuleAction.Mock -> action.throttleDelayMs
                    is RuleAction.Throttle -> action.delayMs
                },
                throttle_delay_max_ms = when (val action = rule.action) {
                    is RuleAction.Mock -> action.throttleDelayMaxMs
                    is RuleAction.Throttle -> action.delayMaxMs
                },
                enabled = if (rule.enabled) 1L else 0L,
                id = rule.id,
            )
        }
    }

    override fun search(query: String): Flow<List<WiretapRule>> {
        return queries.searchRules(query = query)
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun updateEnabled(id: Long, enabled: Boolean) {
        withContext(Dispatchers.IO) {
            queries.updateRuleEnabled(
                enabled = if (enabled) 1L else 0L,
                id = id,
            )
        }
    }

    override suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteRuleById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            queries.deleteAllRules()
        }
    }
}
