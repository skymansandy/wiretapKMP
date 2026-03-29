/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.repository

import dev.skymansandy.wiretap.domain.model.WiretapRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface RuleRepository {

    fun flowAll(): Flow<List<WiretapRule>>

    fun flowForQuery(query: String): Flow<List<WiretapRule>>

    fun flowById(id: Long): Flow<WiretapRule?>

    suspend fun addRule(rule: WiretapRule)

    suspend fun updateRule(rule: WiretapRule)

    suspend fun getById(id: Long): WiretapRule?

    suspend fun getEnabledRules(): List<WiretapRule>

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun deleteById(id: Long)

    suspend fun deleteAll()

    companion object {

        internal val NoOp = object : RuleRepository {

            override fun flowAll() = flowOf(emptyList<WiretapRule>())
            override fun flowForQuery(query: String) = flowOf(emptyList<WiretapRule>())
            override fun flowById(id: Long): Flow<WiretapRule?> = flowOf(null)
            override suspend fun getById(id: Long) = null
            override suspend fun getEnabledRules() = emptyList<WiretapRule>()
            override suspend fun addRule(rule: WiretapRule) {}
            override suspend fun updateRule(rule: WiretapRule) {}
            override suspend fun deleteById(id: Long) {}
            override suspend fun deleteAll() {}
            override suspend fun setEnabled(id: Long, enabled: Boolean) {}
        }
    }
}
