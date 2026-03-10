package dev.skymansandy.wiretap.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.dao.NetworkDao
import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.paging.NetworkLogPagingSource
import dev.skymansandy.wiretap.paging.defaultPagingConfig
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

    override fun getAll(): Flow<List<NetworkLogEntry>> = networkDao.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>> =
        Pager(config = defaultPagingConfig) {
            NetworkLogPagingSource(networkDao, query, invalidationSignal)
        }.flow

    override fun getById(id: Long): NetworkLogEntry? = networkDao.getById(id)

    override fun clearAll() {
        networkDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }
}
