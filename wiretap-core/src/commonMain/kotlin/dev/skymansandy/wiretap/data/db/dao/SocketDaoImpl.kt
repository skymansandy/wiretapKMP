package dev.skymansandy.wiretap.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class SocketDaoImpl(
    private val database: WiretapDatabase,
) : SocketDao {

    private val queries get() = database.wiretapQueries

    override suspend fun insertAndGetId(entry: SocketEntry): Long = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            queries.insertSocketLog(
                url = entry.url,
                request_headers = HeadersSerializerUtil.serialize(entry.requestHeaders),
                status = entry.status.name,
                timestamp = entry.timestamp,
                protocol = entry.protocol,
                remote_address = entry.remoteAddress,
            )
            queries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun insertWithId(entry: SocketEntry) {
        withContext(Dispatchers.IO) {
            queries.insertSocketLogWithId(
                id = entry.id,
                url = entry.url,
                request_headers = HeadersSerializerUtil.serialize(entry.requestHeaders),
                status = entry.status.name,
                message_count = entry.messageCount,
                timestamp = entry.timestamp,
                protocol = entry.protocol,
                remote_address = entry.remoteAddress,
                history_cleared = if (entry.historyCleared) 1L else 0L,
            )
        }
    }

    override suspend fun update(entry: SocketEntry) {
        withContext(Dispatchers.IO) {
            queries.updateSocketLog(
                status = entry.status.name,
                close_code = entry.closeCode?.toLong(),
                close_reason = entry.closeReason,
                failure_message = entry.failureMessage,
                closed_at = entry.closedAt,
                protocol = entry.protocol,
                remote_address = entry.remoteAddress,
                id = entry.id,
            )
        }
    }

    override suspend fun insertMessage(message: SocketMessage) {
        withContext(Dispatchers.IO) {
            queries.insertSocketMessage(
                socket_id = message.socketId,
                direction = message.direction.name,
                content_type = message.contentType.name,
                content = message.content,
                byte_count = message.byteCount,
                timestamp = message.timestamp,
            )
        }
    }

    override suspend fun incrementMessageCount(socketId: Long) {
        withContext(Dispatchers.IO) {
            queries.incrementSocketMessageCount(socketId)
        }
    }

    override suspend fun getById(id: Long): SocketEntry? = withContext(Dispatchers.IO) {
        queries.getSocketLogById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun getMessages(socketId: Long): Flow<List<SocketMessage>> {
        return queries.getSocketMessagesBySocketId(socketId)
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<SocketEntry>> {
        return queries.getAllSocketLogs()
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getPage(
        query: String,
        limit: Long,
        afterId: Long?,
    ): List<SocketEntry> = withContext(Dispatchers.IO) {
        queries.getSocketLogsPage(query, afterId, limit)
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            queries.deleteAllSocketMessages()
            queries.deleteAllSocketLogs()
        }
    }

    override suspend fun deleteClosed() {
        withContext(Dispatchers.IO) {
            queries.deleteClosedSocketMessages()
            queries.deleteClosedSocketLogs()
        }
    }
}
