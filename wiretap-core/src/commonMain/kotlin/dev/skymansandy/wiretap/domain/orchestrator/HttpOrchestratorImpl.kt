package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.launcher.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.launcher.onNetworkLogsCleared
import kotlinx.coroutines.flow.Flow

internal class HttpOrchestratorImpl(
    private val networkRepository: NetworkRepository,
    private val networkLogger: NetworkLogger,
) : HttpOrchestrator {

    override suspend fun logEntry(entry: HttpLogEntry) {

        networkRepository.save(entry)
        networkLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override suspend fun logRequest(entry: HttpLogEntry): Long {

        val id = networkRepository.saveAndGetId(entry)
        val entryWithId = entry.copy(id = id)
        networkLogger.logHttp(entryWithId)
        onNetworkEntryLogged(entryWithId)
        return id
    }

    override suspend fun updateEntry(entry: HttpLogEntry) {

        networkRepository.update(entry)
        networkLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override fun getAllLogs(): Flow<List<HttpLogEntry>> = networkRepository.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        networkRepository.getPagedLogs(query)

    override suspend fun getLogById(id: Long): HttpLogEntry? = networkRepository.getById(id)

    override suspend fun deleteLog(id: Long) {

        networkRepository.deleteById(id)
    }

    override suspend fun clearLogs() {

        networkRepository.clearAll()
        onNetworkLogsCleared()
    }

    override suspend fun purgeLogsOlderThan(cutoffMs: Long) {

        networkRepository.deleteOlderThan(cutoffMs)
    }
}
