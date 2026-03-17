package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.notification.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.notification.onNetworkLogsCleared
import kotlinx.coroutines.flow.Flow

class WiretapOrchestratorImpl(
    private val config: WiretapConfig,
    private val networkRepository: NetworkRepository,
    private val networkLogger: NetworkLogger,
) : WiretapOrchestrator {

    override fun logEntry(entry: NetworkLogEntry) {
        if (!config.enabled) return
        networkRepository.save(entry)
        if (config.loggingEnabled) {
            networkLogger.log(entry)
        }
        onNetworkEntryLogged(entry)
    }

    override fun logRequest(entry: NetworkLogEntry): Long {
        if (!config.enabled) return -1
        val id = networkRepository.saveAndGetId(entry)
        val entryWithId = entry.copy(id = id)
        if (config.loggingEnabled) {
            networkLogger.log(entryWithId)
        }
        onNetworkEntryLogged(entryWithId)
        return id
    }

    override fun updateEntry(entry: NetworkLogEntry) {
        if (!config.enabled) return
        networkRepository.update(entry)
        if (config.loggingEnabled) {
            networkLogger.log(entry)
        }
        onNetworkEntryLogged(entry)
    }

    override fun getAllLogs(): Flow<List<NetworkLogEntry>> = networkRepository.getAll()

    override fun getPagedLogs(query: String): Flow<PagingData<NetworkLogEntry>> =
        networkRepository.getPagedLogs(query)

    override fun getLogById(id: Long): NetworkLogEntry? {
        return networkRepository.getById(id)
    }

    override fun clearLogs() {
        networkRepository.clearAll()
        onNetworkLogsCleared()
    }
}
