package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketRepository {

    suspend fun openConnection(entry: SocketLogEntry): Long

    suspend fun reopenConnection(entry: SocketLogEntry)

    suspend fun updateConnection(entry: SocketLogEntry)

    suspend fun logMessage(message: SocketMessage)

    suspend fun getById(id: Long): SocketLogEntry?

    fun getByIdFlow(id: Long): Flow<SocketLogEntry?>

    fun getMessages(socketId: Long): Flow<List<SocketMessage>>

    fun getAll(): Flow<List<SocketLogEntry>>

    fun getPagedConnections(query: String): Flow<PagingData<SocketLogEntry>>

    suspend fun clearAll()

    suspend fun clearClosed()
}
