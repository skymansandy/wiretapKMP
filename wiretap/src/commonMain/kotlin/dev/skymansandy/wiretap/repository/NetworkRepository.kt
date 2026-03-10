package dev.skymansandy.wiretap.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun save(entry: NetworkLogEntry)
    fun getAll(): Flow<List<NetworkLogEntry>>
    fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>>
    fun getById(id: Long): NetworkLogEntry?
    fun clearAll()
}
