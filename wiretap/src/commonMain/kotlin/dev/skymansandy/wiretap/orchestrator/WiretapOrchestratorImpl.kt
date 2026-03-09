package dev.skymansandy.wiretap.orchestrator

import dev.skymansandy.wiretap.WiretapConfig
import dev.skymansandy.wiretap.logger.NetworkLogger
import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.repository.NetworkRepository
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
    }

    override fun getAllLogs(): Flow<List<NetworkLogEntry>> {
        return networkRepository.getAll()
    }

    override fun getLogById(id: Long): NetworkLogEntry? {
        return networkRepository.getById(id)
    }

    override fun clearLogs() {
        networkRepository.clearAll()
    }
}
