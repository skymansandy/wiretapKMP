package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import kotlinx.coroutines.flow.Flow

interface HttpRepository {

    suspend fun save(entry: HttpLogEntry)

    suspend fun saveAndGetId(entry: HttpLogEntry): Long

    suspend fun update(entry: HttpLogEntry)

    fun getAll(): Flow<List<HttpLogEntry>>

    fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>>

    suspend fun getById(id: Long): HttpLogEntry?

    suspend fun deleteById(id: Long)

    suspend fun deleteOlderThan(timestamp: Long)

    suspend fun clearAll()
}
