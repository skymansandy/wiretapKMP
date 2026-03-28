package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketLogManager {

    fun flowSocketById(id: Long): Flow<SocketConnection?>

    fun flowSocketMessagesById(socketId: Long): Flow<List<SocketMessage>>

    fun flowAllSockets(): Flow<List<SocketConnection>>

    fun flowPagedSocketsForSearchQuery(query: String): Flow<PagingData<SocketConnection>>

    suspend fun createSocket(entry: SocketConnection): Long

    suspend fun updateSocket(entry: SocketConnection)

    suspend fun logSocketMsg(message: SocketMessage)

    suspend fun getSocketById(id: Long): SocketConnection?

    suspend fun clearLogs()
}
