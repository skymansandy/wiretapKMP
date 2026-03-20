package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

internal interface SocketDao {

    suspend fun insertAndGetId(entry: SocketLogEntry): Long

    suspend fun insertWithId(entry: SocketLogEntry)

    suspend fun update(entry: SocketLogEntry)

    suspend fun insertMessage(message: SocketMessage)

    suspend fun incrementMessageCount(socketId: Long)

    suspend fun getById(id: Long): SocketLogEntry?

    fun getMessages(socketId: Long): Flow<List<SocketMessage>>

    fun getAll(): Flow<List<SocketLogEntry>>

    suspend fun getPage(query: String, limit: Long, afterId: Long?): List<SocketLogEntry>

    suspend fun deleteAll()

    suspend fun deleteClosed()
}
