package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.launcher.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.launcher.onNetworkLogsCleared
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import kotlinx.coroutines.flow.Flow

internal class HttpOrchestratorImpl(
    private val httpRepository: HttpRepository,
    private val wiretapLogger: WiretapLogger,
) : HttpOrchestrator {

    override suspend fun logHttp(entry: HttpLogEntry) {
        httpRepository.save(entry)
        wiretapLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override suspend fun logHttpAndGetId(entry: HttpLogEntry): Long {
        val id = httpRepository.saveAndGetId(entry)
        val entryWithId = entry.copy(id = id)
        wiretapLogger.logHttp(entryWithId)
        onNetworkEntryLogged(entryWithId)
        return id
    }

    override suspend fun updateHttp(entry: HttpLogEntry) {
        httpRepository.update(entry)
        wiretapLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override fun getAllHttpLogs(): Flow<List<HttpLogEntry>> = httpRepository.getAll()

    override fun getPagedHttpLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        httpRepository.getPagedLogs(query)

    override suspend fun getHttpLogById(id: Long): HttpLogEntry? = httpRepository.getById(id)

    override suspend fun deleteHttpLog(id: Long) {
        httpRepository.deleteById(id)
    }

    override suspend fun clearHttpLogs() {
        httpRepository.clearAll()
        onNetworkLogsCleared()
    }

    override suspend fun purgeHttpLogsOlderThan(cutoffMs: Long) {
        httpRepository.deleteOlderThan(cutoffMs)
    }

    override suspend fun markHttpCancelledIfInProgress(id: Long) {
        httpRepository.markCancelledIfInProgress(id)
    }
}
