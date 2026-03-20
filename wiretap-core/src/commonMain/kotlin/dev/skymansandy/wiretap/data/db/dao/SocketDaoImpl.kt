package dev.skymansandy.wiretap.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.db.SocketLogEntity
import dev.skymansandy.wiretap.db.SocketMessageEntity
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus
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

    override suspend fun insertAndGetId(entry: SocketLogEntry): Long = withContext(Dispatchers.IO) {
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

    override suspend fun insertWithId(entry: SocketLogEntry) {
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

    override suspend fun update(entry: SocketLogEntry) {
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

    override suspend fun getById(id: Long): SocketLogEntry? = withContext(Dispatchers.IO) {
        queries.getSocketLogById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun getMessages(socketId: Long): Flow<List<SocketMessage>> {
        return queries.getSocketMessagesBySocketId(socketId)
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<SocketLogEntry>> {
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
    ): List<SocketLogEntry> = withContext(Dispatchers.IO) {
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

    private fun SocketLogEntity.toDomain(): SocketLogEntry {
        return SocketLogEntry(
            id = id,
            url = url,
            requestHeaders = HeadersSerializerUtil.deserialize(request_headers),
            status = SocketStatus.valueOf(status),
            closeCode = close_code?.toInt(),
            closeReason = close_reason,
            failureMessage = failure_message,
            messageCount = message_count,
            timestamp = timestamp,
            closedAt = closed_at,
            protocol = protocol,
            remoteAddress = remote_address,
            historyCleared = history_cleared != 0L,
        )
    }

    private fun SocketMessageEntity.toDomain(): SocketMessage {
        return SocketMessage(
            id = id,
            socketId = socket_id,
            direction = SocketMessageDirection.valueOf(direction),
            contentType = SocketContentType.valueOf(content_type),
            content = content,
            byteCount = byte_count,
            timestamp = timestamp,
        )
    }
}
