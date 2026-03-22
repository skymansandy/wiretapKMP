package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import kotlinx.coroutines.flow.Flow

interface HttpOrchestrator {

    suspend fun logHttp(entry: HttpLogEntry)

    suspend fun logHttpAndGetId(entry: HttpLogEntry): Long

    suspend fun updateHttp(entry: HttpLogEntry)

    fun getAllHttpLogs(): Flow<List<HttpLogEntry>>

    fun getPagedHttpLogs(query: String): Flow<PagingData<HttpLogEntry>>

    suspend fun getHttpLogById(id: Long): HttpLogEntry?

    suspend fun deleteHttpLog(id: Long)

    suspend fun clearHttpLogs()

    suspend fun purgeHttpLogsOlderThan(cutoffMs: Long)
}
