package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

internal interface SocketDao {

    suspend fun insertAndGetId(entry: SocketEntry): Long

    suspend fun insertWithId(entry: SocketEntry)

    suspend fun update(entry: SocketEntry)

    suspend fun insertMessage(message: SocketMessage)

    suspend fun incrementMessageCount(socketId: Long)

    suspend fun getById(id: Long): SocketEntry?

    fun getMessages(socketId: Long): Flow<List<SocketMessage>>

    fun getAll(): Flow<List<SocketEntry>>

    suspend fun getPage(query: String, limit: Long, afterId: Long?): List<SocketEntry>

    suspend fun deleteAll()

    suspend fun deleteClosed()
}
