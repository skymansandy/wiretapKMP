package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.HttpLog
import kotlinx.coroutines.flow.Flow

interface HttpLogManager {

    fun flowHttpLogs(): Flow<List<HttpLog>>

    fun flowPagedHttpLogsForSearchQuery(query: String): Flow<PagingData<HttpLog>>

    fun flowHttpLogById(id: Long): Flow<HttpLog?>

    suspend fun logHttp(entry: HttpLog)

    suspend fun logHttpAndGetId(entry: HttpLog): Long

    suspend fun updateHttp(entry: HttpLog)

    suspend fun getHttpLogById(id: Long): HttpLog?

    suspend fun deleteHttpLog(id: Long)

    suspend fun clearHttpLogs()

    suspend fun purgeHttpLogsOlderThan(cutoffMs: Long)

    suspend fun markHttpCancelledIfInProgress(id: Long)
}
