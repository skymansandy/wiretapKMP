/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import kotlinx.coroutines.flow.Flow

interface SseRepository {

    fun flowById(id: Long): Flow<SseConnection?>

    fun flowEventsForId(connectionId: Long): Flow<List<SseEvent>>

    fun flowAll(): Flow<List<SseConnection>>

    fun flowForSearchQuery(query: String): Flow<PagingData<SseConnection>>

    suspend fun logNew(connection: SseConnection): Long

    suspend fun markReopened(connection: SseConnection)

    suspend fun update(connection: SseConnection)

    suspend fun logEvent(event: SseEvent)

    suspend fun getById(id: Long): SseConnection?

    suspend fun clearAll()

    suspend fun clearClosed()
}
