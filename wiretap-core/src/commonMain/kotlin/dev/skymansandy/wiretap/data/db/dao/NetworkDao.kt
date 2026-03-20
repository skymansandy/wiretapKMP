package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

internal interface NetworkDao {
    fun insert(entry: NetworkLogEntry)
    fun insertAndGetId(entry: NetworkLogEntry): Long
    fun update(entry: NetworkLogEntry)
    fun getAll(): Flow<List<NetworkLogEntry>>
    fun getPage(query: String, limit: Long, afterId: Long?): List<NetworkLogEntry>
    fun getById(id: Long): NetworkLogEntry?
    fun deleteAll()
    fun deleteById(id: Long)
    fun deleteOlderThan(timestamp: Long)
}
