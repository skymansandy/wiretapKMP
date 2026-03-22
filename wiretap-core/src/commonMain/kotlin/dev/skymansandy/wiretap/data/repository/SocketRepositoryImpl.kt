package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal class SocketRepositoryImpl(
    private val socketDao: SocketDao,
) : SocketRepository {

    internal val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun openConnection(entry: SocketEntry): Long {
        val id = socketDao.insertAndGetId(entry)
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun reopenConnection(entry: SocketEntry) {
        socketDao.insertWithId(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun updateConnection(entry: SocketEntry) {
        socketDao.update(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun logMessage(message: SocketMessage) {
        socketDao.insertMessage(message)
        socketDao.incrementMessageCount(message.socketId)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun getById(id: Long): SocketEntry? = socketDao.getById(id)

    override fun getByIdFlow(id: Long): Flow<SocketEntry?> =
        invalidationSignal
            .onStart { emit(Unit) }
            .map { socketDao.getById(id) }

    override fun getMessages(socketId: Long): Flow<List<SocketMessage>> = socketDao.getMessages(socketId)

    override fun getAll(): Flow<List<SocketEntry>> = socketDao.getAll()

    override fun getPagedConnections(query: String): Flow<PagingData<SocketEntry>> =
        Pager(config = defaultPagingConfig) {
            SocketLogPagingSource(
                dao = socketDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun clearAll() {
        socketDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearClosed() {
        socketDao.deleteClosed()
        invalidationSignal.tryEmit(Unit)
    }
}
