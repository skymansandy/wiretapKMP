package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.data.db.room.dao.SocketLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class SocketRepositoryImpl(
    private val socketLogsDao: SocketLogsDao,
) : SocketRepository {

    internal val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun openConnection(entry: SocketEntry): Long {
        val id = socketLogsDao.insertSocketLog(entry.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun reopenConnection(entry: SocketEntry) {
        socketLogsDao.insertSocketLogWithId(entry.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun updateConnection(entry: SocketEntry) {
        socketLogsDao.updateSocketLog(
            status = entry.status.name,
            closeCode = entry.closeCode?.toLong(),
            closeReason = entry.closeReason,
            failureMessage = entry.failureMessage,
            closedAt = entry.closedAt,
            protocol = entry.protocol,
            remoteAddress = entry.remoteAddress,
            id = entry.id,
        )
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun logMessage(message: SocketMessage) {
        socketLogsDao.insertSocketMessage(
            SocketMessageEntity(
                socketId = message.socketId,
                direction = message.direction.name,
                contentType = message.contentType.name,
                content = message.content,
                byteCount = message.byteCount,
                timestamp = message.timestamp,
            ),
        )
        socketLogsDao.incrementSocketMessageCount(message.socketId)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun getById(id: Long): SocketEntry? =
        socketLogsDao.getSocketLogById(id)?.toDomain()

    override fun getByIdFlow(id: Long): Flow<SocketEntry?> =
        invalidationSignal
            .onStart { emit(Unit) }
            .map { socketLogsDao.getSocketLogById(id)?.toDomain() }

    override fun getMessages(socketId: Long): Flow<List<SocketMessage>> =
        socketLogsDao.getSocketMessagesBySocketId(socketId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun getAll(): Flow<List<SocketEntry>> =
        socketLogsDao.getAllSocketLogs()
            .map { entities -> entities.map { it.toDomain() } }

    override fun getPagedConnections(query: String): Flow<PagingData<SocketEntry>> =
        Pager(config = defaultPagingConfig) {
            SocketLogPagingSource(
                roomDao = socketLogsDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun clearAll() {
        socketLogsDao.deleteAllSocketMessages()
        socketLogsDao.deleteAllSocketLogs()
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearClosed() {
        socketLogsDao.deleteClosedSocketMessages()
        socketLogsDao.deleteClosedSocketLogs()
        invalidationSignal.tryEmit(Unit)
    }
}

private fun SocketEntry.toRoomEntity(): SocketLogEntity {
    return SocketLogEntity(
        id = id,
        url = url,
        requestHeaders = HeadersSerializerUtil.serialize(requestHeaders),
        status = status.name,
        closeCode = closeCode?.toLong(),
        closeReason = closeReason,
        failureMessage = failureMessage,
        messageCount = messageCount,
        timestamp = timestamp,
        closedAt = closedAt,
        protocol = protocol,
        remoteAddress = remoteAddress,
        historyCleared = if (historyCleared) 1L else 0L,
    )
}
