/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.room.dao.SseLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.SseEventEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.data.mappers.toRoomEntity
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.repository.SseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class SseRepositoryImpl(
    private val sseLogsDao: SseLogsDao,
) : SseRepository {

    internal val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun flowById(id: Long): Flow<SseConnection?> =
        invalidationSignal
            .onStart { emit(Unit) }
            .map { sseLogsDao.getSseLogById(id)?.toDomain() }

    override fun flowEventsForId(connectionId: Long): Flow<List<SseEvent>> =
        sseLogsDao.getSseEventsByConnectionId(connectionId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun flowAll(): Flow<List<SseConnection>> =
        sseLogsDao.getAllSseLogs()
            .map { entities -> entities.map { it.toDomain() } }

    override fun flowForSearchQuery(query: String): Flow<PagingData<SseConnection>> =
        Pager(config = defaultPagingConfig) {
            SseLogPagingSource(
                roomDao = sseLogsDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun logNew(connection: SseConnection): Long {
        val id = sseLogsDao.insertSseLog(connection.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun markReopened(connection: SseConnection) {
        sseLogsDao.insertSseLogWithId(connection.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun update(connection: SseConnection) {
        sseLogsDao.updateSseLog(
            status = connection.status.name,
            failureMessage = connection.failureMessage,
            closedAt = connection.closedAt,
            lastEventId = connection.lastEventId,
            retryMs = connection.retryMs,
            id = connection.id,
        )
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun logEvent(event: SseEvent) {
        sseLogsDao.insertSseEvent(
            SseEventEntity(
                connectionId = event.connectionId,
                eventType = event.eventType,
                data = event.data,
                eventId = event.eventId,
                retryMs = event.retryMs,
                byteCount = event.byteCount,
                timestamp = event.timestamp,
            ),
        )
        sseLogsDao.incrementSseEventCount(event.connectionId)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun getById(id: Long): SseConnection? =
        sseLogsDao.getSseLogById(id)?.toDomain()

    override suspend fun clearAll() {
        sseLogsDao.deleteAllSseEvents()
        sseLogsDao.deleteAllSseLogs()
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearClosed() {
        sseLogsDao.deleteClosedSseEvents()
        sseLogsDao.deleteClosedSseLogs()
        invalidationSignal.tryEmit(Unit)
    }
}
