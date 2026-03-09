package dev.skymansandy.wiretap.repository

import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun save(entry: NetworkLogEntry)
    fun getAll(): Flow<List<NetworkLogEntry>>
    fun getById(id: Long): NetworkLogEntry?
    fun clearAll()
}
