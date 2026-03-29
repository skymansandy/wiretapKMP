/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.data.mappers.toRoomEntity
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

internal class HttpRepositoryImpl(
    private val httpLogsDao: HttpLogsDao,
) : HttpRepository {

    private val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun flowAll(): Flow<List<HttpLog>> = httpLogsDao.getAllNetworkLogs().map { rows ->
        rows.map { it.toDomain() }
    }

    override fun flowDistinctHosts(): Flow<List<String>> = httpLogsDao.getDistinctHosts()

    override fun flowPagesLogs(query: String, filter: HttpLogFilter): Flow<PagingData<HttpLog>> =
        Pager(config = defaultPagingConfig) {
            HttpLogPagingSource(
                roomDao = httpLogsDao,
                query = query,
                filter = filter,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override fun flowById(id: Long): Flow<HttpLog?> =
        httpLogsDao.flowById(id).map { it?.toDomain() }

    override suspend fun save(log: HttpLog) {
        httpLogsDao.insert(log.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun saveAndGetId(log: HttpLog): Long {
        val id = httpLogsDao.insert(log.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun update(log: HttpLog) {
        httpLogsDao.update(
            responseCode = log.responseCode.toLong(),
            responseHeaders = HeadersSerializerUtil.serialize(log.responseHeaders),
            responseBody = log.responseBody,
            durationMs = log.durationMs,
            source = log.source.name,
            matchedRuleId = log.matchedRuleId,
            protocol = log.protocol,
            remoteAddress = log.remoteAddress,
            tlsProtocol = log.tlsProtocol,
            cipherSuite = log.cipherSuite,
            certificateCn = log.certificateCn,
            issuerCn = log.issuerCn,
            certificateExpiry = log.certificateExpiry,
            id = log.id,
        )
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun getById(id: Long): HttpLog? =
        httpLogsDao.getById(id)?.toDomain()

    override suspend fun deleteById(id: Long) {
        httpLogsDao.deleteById(id)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        httpLogsDao.deleteOlderThan(timestamp)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearAll() {
        httpLogsDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun markCancelledIfInProgress(id: Long) {
        httpLogsDao.markCancelledIfInProgress(id)
        invalidationSignal.tryEmit(Unit)
    }
}
