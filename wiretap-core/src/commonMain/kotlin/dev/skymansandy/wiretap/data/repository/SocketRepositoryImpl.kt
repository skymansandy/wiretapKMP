package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.room.dao.SocketLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.data.mappers.toRoomEntity
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class SocketRepositoryImpl(
    private val socketLogsDao: SocketLogsDao,
) : SocketRepository {

    internal val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun flowById(id: Long): Flow<SocketConnection?> =
        invalidationSignal
            .onStart { emit(Unit) }
            .map { socketLogsDao.getSocketLogById(id)?.toDomain() }

    override fun flowMessagesForId(socketId: Long): Flow<List<SocketMessage>> =
        socketLogsDao.getSocketMessagesBySocketId(socketId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun flowAll(): Flow<List<SocketConnection>> =
        socketLogsDao.getAllSocketLogs()
            .map { entities -> entities.map { it.toDomain() } }

    override fun flowForSearchQuery(query: String): Flow<PagingData<SocketConnection>> =
        Pager(config = defaultPagingConfig) {
            SocketLogPagingSource(
                roomDao = socketLogsDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun logNew(socket: SocketConnection): Long {
        val id = socketLogsDao.insertSocketLog(socket.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun markReopened(socket: SocketConnection) {
        socketLogsDao.insertSocketLogWithId(socket.toRoomEntity())
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun update(socket: SocketConnection) {
        socketLogsDao.updateSocketLog(
            status = socket.status.name,
            closeCode = socket.closeCode?.toLong(),
            closeReason = socket.closeReason,
            failureMessage = socket.failureMessage,
            closedAt = socket.closedAt,
            protocol = socket.protocol,
            remoteAddress = socket.remoteAddress,
            id = socket.id,
        )
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun logSocketMsg(message: SocketMessage) {
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

    override suspend fun getById(id: Long): SocketConnection? =
        socketLogsDao.getSocketLogById(id)?.toDomain()

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
