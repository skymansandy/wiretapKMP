package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface WiretapOrchestrator {
    fun logEntry(entry: NetworkLogEntry)
    fun getAllLogs(): Flow<List<NetworkLogEntry>>
    fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>>
    fun getLogById(id: Long): NetworkLogEntry?
    fun clearLogs()
}
