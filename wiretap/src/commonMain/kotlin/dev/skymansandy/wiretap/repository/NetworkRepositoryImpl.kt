package dev.skymansandy.wiretap.repository

import dev.skymansandy.wiretap.dao.NetworkDao
import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

class NetworkRepositoryImpl(
    private val networkDao: NetworkDao,
) : NetworkRepository {

    override fun save(entry: NetworkLogEntry) {
        networkDao.insert(entry)
    }

    override fun getAll(): Flow<List<NetworkLogEntry>> {
        return networkDao.getAll()
    }

    override fun getById(id: Long): NetworkLogEntry? {
        return networkDao.getById(id)
    }

    override fun clearAll() {
        networkDao.deleteAll()
    }
}
