/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import kotlinx.coroutines.flow.Flow

interface SseLogManager {

    fun flowConnectionById(id: Long): Flow<SseConnection?>

    fun flowEventsById(connectionId: Long): Flow<List<SseEvent>>

    fun flowAllConnections(): Flow<List<SseConnection>>

    fun flowPagedConnectionsForSearchQuery(query: String): Flow<PagingData<SseConnection>>

    suspend fun createConnection(entry: SseConnection): Long

    suspend fun updateConnection(entry: SseConnection)

    suspend fun logEvent(event: SseEvent)

    suspend fun getConnectionById(id: Long): SseConnection?

    suspend fun clearLogs()
}
