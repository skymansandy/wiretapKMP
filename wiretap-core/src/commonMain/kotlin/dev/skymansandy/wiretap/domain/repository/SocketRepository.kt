package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketRepository {

    suspend fun openConnection(entry: SocketEntry): Long

    suspend fun reopenConnection(entry: SocketEntry)

    suspend fun updateConnection(entry: SocketEntry)

    suspend fun logMessage(message: SocketMessage)

    suspend fun getById(id: Long): SocketEntry?

    fun getByIdFlow(id: Long): Flow<SocketEntry?>

    fun getMessages(socketId: Long): Flow<List<SocketMessage>>

    fun getAll(): Flow<List<SocketEntry>>

    fun getPagedConnections(query: String): Flow<PagingData<SocketEntry>>

    suspend fun clearAll()

    suspend fun clearClosed()
}
