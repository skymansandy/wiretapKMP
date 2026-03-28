package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.HttpLog
import kotlinx.coroutines.flow.Flow

interface HttpRepository {

    fun flowAll(): Flow<List<HttpLog>>

    fun flowPagesLogs(query: String): Flow<PagingData<HttpLog>>

    fun flowById(id: Long): Flow<HttpLog?>

    suspend fun save(log: HttpLog)

    suspend fun saveAndGetId(log: HttpLog): Long

    suspend fun update(log: HttpLog)

    suspend fun getById(id: Long): HttpLog?

    suspend fun deleteById(id: Long)

    suspend fun deleteOlderThan(timestamp: Long)

    suspend fun clearAll()

    suspend fun markCancelledIfInProgress(id: Long)
}
