package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

internal class HttpRepositoryImpl(
    private val httpLogsDao: HttpLogsDao,
) : HttpRepository {

    private val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun save(entry: HttpLogEntry) {
        httpLogsDao.insert(entry.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun saveAndGetId(entry: HttpLogEntry): Long {
        val id = httpLogsDao.insert(entry.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun update(entry: HttpLogEntry) {
        httpLogsDao.update(
            responseCode = entry.responseCode.toLong(),
            responseHeaders = HeadersSerializerUtil.serialize(entry.responseHeaders),
            responseBody = entry.responseBody,
            durationMs = entry.durationMs,
            source = entry.source.name,
            matchedRuleId = entry.matchedRuleId,
            protocol = entry.protocol,
            remoteAddress = entry.remoteAddress,
            tlsProtocol = entry.tlsProtocol,
            cipherSuite = entry.cipherSuite,
            certificateCn = entry.certificateCn,
            issuerCn = entry.issuerCn,
            certificateExpiry = entry.certificateExpiry,
            id = entry.id,
        )
        invalidationSignal.tryEmit(Unit)
    }

    override fun getAll(): Flow<List<HttpLogEntry>> =
        httpLogsDao.getAllNetworkLogs().map { rows -> rows.map { it.toDomain() } }

    override fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        Pager(config = defaultPagingConfig) {
            HttpLogPagingSource(
                roomDao = httpLogsDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun getById(id: Long): HttpLogEntry? =
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

private fun HttpLogEntry.toRoomEntity(): HttpLogEntity {
    return HttpLogEntity(
        id = id,
        url = url,
        method = method,
        requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
        requestBody = requestBody,
        responseCode = responseCode.toLong(),
        responseHeaders = HeadersSerializerUtil.serialize(responseHeaders),
        responseBody = responseBody,
        durationMs = durationMs,
        source = source.name,
        timestamp = timestamp,
        matchedRuleId = matchedRuleId,
        protocol = protocol,
        remoteAddress = remoteAddress,
        tlsProtocol = tlsProtocol,
        cipherSuite = cipherSuite,
        certificateCn = certificateCn,
        issuerCn = issuerCn,
        certificateExpiry = certificateExpiry,
    )
}
