package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketOrchestrator {

    suspend fun createSocket(entry: SocketEntry): Long

    suspend fun updateSocket(entry: SocketEntry)

    suspend fun logSocketMsg(message: SocketMessage)

    suspend fun getSocketById(id: Long): SocketEntry?

    fun flowSocketById(id: Long): Flow<SocketEntry?>

    fun flowSocketMessagesById(socketId: Long): Flow<List<SocketMessage>>

    fun getAllSockets(): Flow<List<SocketEntry>>

    fun getPagedSockets(query: String): Flow<PagingData<SocketEntry>>

    suspend fun clearLogs()
}
