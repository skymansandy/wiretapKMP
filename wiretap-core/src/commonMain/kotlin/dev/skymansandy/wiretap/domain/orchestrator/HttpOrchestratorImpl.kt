package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.helper.launcher.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.launcher.onNetworkLogsCleared
import kotlinx.coroutines.flow.Flow

internal class HttpOrchestratorImpl(
    private val httpRepository: HttpRepository,
    private val wiretapLogger: WiretapLogger,
) : HttpOrchestrator {

    override suspend fun logEntry(entry: HttpLogEntry) {

        httpRepository.save(entry)
        wiretapLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override suspend fun logRequest(entry: HttpLogEntry): Long {

        val id = httpRepository.saveAndGetId(entry)
        val entryWithId = entry.copy(id = id)
        wiretapLogger.logHttp(entryWithId)
        onNetworkEntryLogged(entryWithId)
        return id
    }

    override suspend fun updateEntry(entry: HttpLogEntry) {

        httpRepository.update(entry)
        wiretapLogger.logHttp(entry)
        onNetworkEntryLogged(entry)
    }

    override fun getAllLogs(): Flow<List<HttpLogEntry>> = httpRepository.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>> =
        httpRepository.getPagedLogs(query)

    override suspend fun getLogById(id: Long): HttpLogEntry? = httpRepository.getById(id)

    override suspend fun deleteLog(id: Long) {

        httpRepository.deleteById(id)
    }

    override suspend fun clearLogs() {

        httpRepository.clearAll()
        onNetworkLogsCleared()
    }

    override suspend fun purgeLogsOlderThan(cutoffMs: Long) {

        httpRepository.deleteOlderThan(cutoffMs)
    }
}
