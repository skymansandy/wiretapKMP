package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface WiretapOrchestrator {

    suspend fun logEntry(entry: HttpLogEntry)

    suspend fun logRequest(entry: HttpLogEntry): Long

    suspend fun updateEntry(entry: HttpLogEntry)

    fun getAllLogs(): Flow<List<HttpLogEntry>>

    fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>>

    suspend fun getLogById(id: Long): HttpLogEntry?

    suspend fun deleteLog(id: Long)

    suspend fun clearLogs()

    suspend fun purgeLogsOlderThan(cutoffMs: Long)

    // Socket

    suspend fun openSocketConnection(entry: SocketLogEntry): Long

    suspend fun updateSocketConnection(entry: SocketLogEntry)

    suspend fun logSocketMessage(message: SocketMessage)

    suspend fun getSocketById(id: Long): SocketLogEntry?

    fun getSocketByIdFlow(id: Long): Flow<SocketLogEntry?>

    fun getSocketMessages(socketId: Long): Flow<List<SocketMessage>>

    fun getAllSocketLogs(): Flow<List<SocketLogEntry>>

    fun getPagedSocketLogs(query: String): Flow<PagingData<SocketLogEntry>>

    suspend fun clearSocketLogs()
}
