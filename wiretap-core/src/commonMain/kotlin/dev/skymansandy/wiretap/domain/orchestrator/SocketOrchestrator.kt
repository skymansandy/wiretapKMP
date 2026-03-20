package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketOrchestrator {

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
