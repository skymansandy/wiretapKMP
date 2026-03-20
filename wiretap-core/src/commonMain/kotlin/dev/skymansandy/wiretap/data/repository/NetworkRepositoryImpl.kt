package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.dao.NetworkDao
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class NetworkRepositoryImpl(
    private val networkDao: NetworkDao,
) : NetworkRepository {

    private val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun save(entry: HttpLogEntry) {
        networkDao.insert(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun saveAndGetId(entry: HttpLogEntry): Long {
        val id = networkDao.insertAndGetId(entry)
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun update(entry: HttpLogEntry) {
        networkDao.update(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun getAll(): Flow<List<HttpLogEntry>> = networkDao.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        Pager(config = defaultPagingConfig) {
            NetworkLogPagingSource(
                dao = networkDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun getById(id: Long): HttpLogEntry? = networkDao.getById(id)

    override suspend fun deleteById(id: Long) {
        networkDao.deleteById(id)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun deleteOlderThan(timestamp: Long) {

        networkDao.deleteOlderThan(timestamp)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearAll() {
        networkDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }
}
