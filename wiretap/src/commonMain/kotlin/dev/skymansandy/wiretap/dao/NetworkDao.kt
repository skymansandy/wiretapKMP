package dev.skymansandy.wiretap.dao

import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface NetworkDao {
    fun insert(entry: NetworkLogEntry)
    fun getAll(): Flow<List<NetworkLogEntry>>
    fun getPage(query: String, limit: Long, offset: Long): List<NetworkLogEntry>
    fun getById(id: Long): NetworkLogEntry?
    fun deleteAll()
    fun deleteById(id: Long)
}
