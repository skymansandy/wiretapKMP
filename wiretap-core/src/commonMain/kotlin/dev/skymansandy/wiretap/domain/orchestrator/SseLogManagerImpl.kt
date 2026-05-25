/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import co.touchlab.stately.collections.ConcurrentMutableMap
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.domain.repository.SseRepository
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationHook
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import kotlinx.coroutines.flow.Flow

internal class SseLogManagerImpl(
    private val sseRepository: SseRepository,
    private val wiretapLogger: WiretapLogger,
    private val notificationHook: WiretapNotificationHook,
) : SseLogManager {

    // Cache of active (OPEN/CONNECTING) SSE connections, used to re-create entries after log clear
    private val activeConnections = ConcurrentMutableMap<Long, SseConnection>()

    override suspend fun createConnection(entry: SseConnection): Long {
        val id = sseRepository.logNew(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        wiretapLogger.logSse(entryWithId)
        notificationHook.onNewSseConnection(entryWithId)
        return id
    }

    override suspend fun updateConnection(entry: SseConnection) {
        sseRepository.update(entry)
        when (entry.status) {
            SseStatus.Closed,
            SseStatus.Failed,
            -> activeConnections.remove(entry.id)

            else -> activeConnections[entry.id] = entry
        }
        wiretapLogger.logSse(entry)
        notificationHook.onNewSseConnection(entry)
    }

    override suspend fun logEvent(event: SseEvent) {
        // If the connection entry was cleared but the connection is still active, re-create it
        val existingEntry = sseRepository.getById(event.connectionId)
        if (existingEntry == null) {
            val cached = activeConnections[event.connectionId]
            if (cached != null) {
                val reopened = cached.copy(historyCleared = true, eventCount = 0)
                sseRepository.markReopened(reopened)
                notificationHook.onNewSseConnection(reopened)
            }
        }

        sseRepository.logEvent(event)
        wiretapLogger.logSseEvent(event)
        sseRepository.getById(event.connectionId)?.let { entry ->
            notificationHook.onNewSseEvent(entry, event)
        }
    }

    override suspend fun getConnectionById(id: Long): SseConnection? = sseRepository.getById(id)

    override fun flowConnectionById(id: Long): Flow<SseConnection?> = sseRepository.flowById(id)

    override fun flowEventsById(connectionId: Long): Flow<List<SseEvent>> =
        sseRepository.flowEventsForId(connectionId)

    override fun flowAllConnections(): Flow<List<SseConnection>> = sseRepository.flowAll()

    override fun flowPagedConnectionsForSearchQuery(query: String): Flow<PagingData<SseConnection>> =
        sseRepository.flowForSearchQuery(query)

    override suspend fun clearLogs() {
        sseRepository.clearAll()
        notificationHook.onClearSseLogs()
    }
}
