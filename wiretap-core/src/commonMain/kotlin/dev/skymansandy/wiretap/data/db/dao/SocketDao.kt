package dev.skymansandy.wiretap.data.db.dao

import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import kotlinx.coroutines.flow.Flow

internal interface SocketDao {
    fun insertAndGetId(entry: SocketLogEntry): Long
    fun insertWithId(entry: SocketLogEntry)
    fun update(entry: SocketLogEntry)
    fun insertMessage(message: SocketMessage)
    fun incrementMessageCount(socketId: Long)
    fun getById(id: Long): SocketLogEntry?
    fun getMessages(socketId: Long): Flow<List<SocketMessage>>
    fun getAll(): Flow<List<SocketLogEntry>>
    fun getPage(query: String, limit: Long, afterId: Long?): List<SocketLogEntry>
    fun deleteAll()
    fun deleteClosed()
}
