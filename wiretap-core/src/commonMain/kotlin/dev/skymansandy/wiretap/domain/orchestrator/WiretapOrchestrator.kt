package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface WiretapOrchestrator {
    fun logEntry(entry: NetworkLogEntry)
    fun logRequest(entry: NetworkLogEntry): Long
    fun updateEntry(entry: NetworkLogEntry)
    fun getAllLogs(): Flow<List<NetworkLogEntry>>
    fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>>
    fun getLogById(id: Long): NetworkLogEntry?
    fun deleteLog(id: Long)
    fun clearLogs()
    fun purgeLogsOlderThan(cutoffMs: Long)

    // Socket
    fun openSocketConnection(entry: SocketLogEntry): Long
    fun updateSocketConnection(entry: SocketLogEntry)
    fun logSocketMessage(message: SocketMessage)
    fun getSocketById(id: Long): SocketLogEntry?
    fun getSocketByIdFlow(id: Long): Flow<SocketLogEntry?>
    fun getSocketMessages(socketId: Long): Flow<List<SocketMessage>>
    fun getAllSocketLogs(): Flow<List<SocketLogEntry>>
    fun getPagedSocketLogs(query: String): Flow<PagingData<SocketLogEntry>>
    fun clearSocketLogs()
}
