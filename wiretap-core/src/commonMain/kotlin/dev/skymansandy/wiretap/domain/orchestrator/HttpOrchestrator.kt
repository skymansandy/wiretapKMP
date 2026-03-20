package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import kotlinx.coroutines.flow.Flow

interface HttpOrchestrator {

    suspend fun logEntry(entry: HttpLogEntry)

    suspend fun logRequest(entry: HttpLogEntry): Long

    suspend fun updateEntry(entry: HttpLogEntry)

    fun getAllLogs(): Flow<List<HttpLogEntry>>

    fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>>

    suspend fun getLogById(id: Long): HttpLogEntry?

    suspend fun deleteLog(id: Long)

    suspend fun clearLogs()

    suspend fun purgeLogsOlderThan(cutoffMs: Long)
}
