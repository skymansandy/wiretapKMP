package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import kotlinx.coroutines.flow.Flow

internal interface HttpDao {

    suspend fun insert(entry: HttpLogEntry)

    suspend fun insertAndGetId(entry: HttpLogEntry): Long

    suspend fun update(entry: HttpLogEntry)

    fun getAll(): Flow<List<HttpLogEntry>>

    suspend fun getPage(query: String, limit: Long, afterId: Long?): List<HttpLogEntry>

    suspend fun getById(id: Long): HttpLogEntry?

    suspend fun deleteAll()

    suspend fun deleteById(id: Long)

    suspend fun deleteOlderThan(timestamp: Long)

    suspend fun markCancelledIfInProgress(id: Long)
}
