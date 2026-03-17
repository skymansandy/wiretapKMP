package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    fun save(entry: NetworkLogEntry)
    fun saveAndGetId(entry: NetworkLogEntry): Long
    fun update(entry: NetworkLogEntry)
    fun getAll(): Flow<List<NetworkLogEntry>>
    fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>>
    fun getById(id: Long): NetworkLogEntry?
    fun clearAll()
}
