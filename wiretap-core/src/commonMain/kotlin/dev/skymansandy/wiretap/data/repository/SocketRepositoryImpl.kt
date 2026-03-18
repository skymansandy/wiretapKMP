package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SocketRepositoryImpl(
    private val socketDao: SocketDao,
) : SocketRepository {

    internal val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun openConnection(entry: SocketLogEntry): Long {
        val id = socketDao.insertAndGetId(entry)
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override fun reopenConnection(entry: SocketLogEntry) {
        socketDao.insertWithId(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun updateConnection(entry: SocketLogEntry) {
        socketDao.update(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun logMessage(message: SocketMessage) {
        socketDao.insertMessage(message)
        socketDao.incrementMessageCount(message.socketId)
        invalidationSignal.tryEmit(Unit)
    }

    override fun getById(id: Long): SocketLogEntry? = socketDao.getById(id)

    override fun getByIdFlow(id: Long): Flow<SocketLogEntry?> =
        invalidationSignal
            .onStart { emit(Unit) }
            .map { socketDao.getById(id) }

    override fun getMessages(socketId: Long): Flow<List<SocketMessage>> = socketDao.getMessages(socketId)

    override fun getAll(): Flow<List<SocketLogEntry>> = socketDao.getAll()

    override fun getPagedConnections(query: String): Flow<PagingData<SocketLogEntry>> =
        Pager(config = defaultPagingConfig) {
            SocketLogPagingSource(socketDao, query, invalidationSignal)
        }.flow

    override fun clearAll() {
        socketDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }

    override fun clearClosed() {
        socketDao.deleteClosed()
        invalidationSignal.tryEmit(Unit)
    }
}
