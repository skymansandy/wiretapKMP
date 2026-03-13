package dev.skymansandy.wiretap.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.db.RuleEntity
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.model.HeadersSerializer
import dev.skymansandy.wiretap.model.MatcherType
import dev.skymansandy.wiretap.model.RuleAction
import dev.skymansandy.wiretap.model.WiretapRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleDaoImpl(
    private val database: WiretapDatabase,
) : RuleDao {

    private val queries get() = database.wiretapQueries

    override fun insert(rule: WiretapRule) {
        queries.insertRule(
            matcher_type = rule.matcherType.name,
            url_pattern = rule.urlPattern,
            method = rule.method,
            action = rule.action.name,
            mock_response_code = rule.mockResponseCode?.toLong(),
            mock_response_body = rule.mockResponseBody,
            mock_response_headers = rule.mockResponseHeaders?.let { HeadersSerializer.serialize(it) },
            throttle_delay_ms = rule.throttleDelayMs,
            enabled = if (rule.enabled) 1L else 0L,
            created_at = rule.createdAt,
        )
    }

    override fun getAll(): Flow<List<WiretapRule>> {
        return queries.getAllRules()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getById(id: Long): WiretapRule? {
        return queries.getRuleById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun getEnabledRules(): List<WiretapRule> {
        return queries.getEnabledRules().executeAsList().map { it.toDomain() }
    }

    override fun update(rule: WiretapRule) {
        queries.updateRule(
            matcher_type = rule.matcherType.name,
            url_pattern = rule.urlPattern,
            method = rule.method,
            action = rule.action.name,
            mock_response_code = rule.mockResponseCode?.toLong(),
            mock_response_body = rule.mockResponseBody,
            mock_response_headers = rule.mockResponseHeaders?.let { HeadersSerializer.serialize(it) },
            throttle_delay_ms = rule.throttleDelayMs,
            enabled = if (rule.enabled) 1L else 0L,
            id = rule.id,
        )
    }

    override fun search(query: String): Flow<List<WiretapRule>> {
        return queries.searchRules(query = query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun updateEnabled(id: Long, enabled: Boolean) {
        queries.updateRuleEnabled(enabled = if (enabled) 1L else 0L, id = id)
    }

    override fun deleteById(id: Long) {
        queries.deleteRuleById(id)
    }

    override fun deleteAll() {
        queries.deleteAllRules()
    }

    private fun RuleEntity.toDomain(): WiretapRule {
        return WiretapRule(
            id = id,
            matcherType = runCatching { MatcherType.valueOf(matcher_type) }
                .getOrDefault(MatcherType.URL_EXACT),
            urlPattern = url_pattern,
            method = method,
            action = RuleAction.valueOf(action),
            mockResponseCode = mock_response_code?.toInt(),
            mockResponseBody = mock_response_body,
            mockResponseHeaders = mock_response_headers?.let { HeadersSerializer.deserialize(it) },
            throttleDelayMs = throttle_delay_ms,
            enabled = enabled == 1L,
            createdAt = created_at,
        )
    }
}
