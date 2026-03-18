package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.dao.NetworkDao
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class NetworkRepositoryImpl(
    private val networkDao: NetworkDao,
) : NetworkRepository {

    private val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun save(entry: NetworkLogEntry) {
        networkDao.insert(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun saveAndGetId(entry: NetworkLogEntry): Long {
        val id = networkDao.insertAndGetId(entry)
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override fun update(entry: NetworkLogEntry) {
        networkDao.update(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun getAll(): Flow<List<NetworkLogEntry>> = networkDao.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>> =
        Pager(config = defaultPagingConfig) {
            NetworkLogPagingSource(networkDao, query, invalidationSignal)
        }.flow

    override fun getById(id: Long): NetworkLogEntry? = networkDao.getById(id)

    override fun deleteById(id: Long) {
        networkDao.deleteById(id)
        invalidationSignal.tryEmit(Unit)
    }

    override fun deleteOlderThan(timestamp: Long) {
        networkDao.deleteOlderThan(timestamp)
        invalidationSignal.tryEmit(Unit)
    }

    override fun clearAll() {
        networkDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }
}
