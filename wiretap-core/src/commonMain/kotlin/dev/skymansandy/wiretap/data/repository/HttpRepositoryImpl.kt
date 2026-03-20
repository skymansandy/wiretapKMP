package dev.skymansandy.wiretap.data.repository

import app.cash.paging.Pager
import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.dao.HttpDao
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class HttpRepositoryImpl(
    private val httpDao: HttpDao,
) : HttpRepository {

    private val invalidationSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun save(entry: HttpLogEntry) {
        httpDao.insert(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun saveAndGetId(entry: HttpLogEntry): Long {
        val id = httpDao.insertAndGetId(entry)
        invalidationSignal.tryEmit(Unit)
        return id
    }

    override suspend fun update(entry: HttpLogEntry) {
        httpDao.update(entry)
        invalidationSignal.tryEmit(Unit)
    }

    override fun getAll(): Flow<List<HttpLogEntry>> = httpDao.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        Pager(config = defaultPagingConfig) {
            HttpLogPagingSource(
                dao = httpDao,
                query = query,
                invalidationSignal = invalidationSignal,
            )
        }.flow

    override suspend fun getById(id: Long): HttpLogEntry? = httpDao.getById(id)

    override suspend fun deleteById(id: Long) {
        httpDao.deleteById(id)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun deleteOlderThan(timestamp: Long) {

        httpDao.deleteOlderThan(timestamp)
        invalidationSignal.tryEmit(Unit)
    }

    override suspend fun clearAll() {
        httpDao.deleteAll()
        invalidationSignal.tryEmit(Unit)
    }
}
