package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.notification.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.notification.onNetworkLogsCleared
import dev.skymansandy.wiretap.helper.notification.onSocketConnectionLogged
import dev.skymansandy.wiretap.helper.notification.onSocketLogsCleared
import kotlinx.coroutines.flow.Flow

class WiretapOrchestratorImpl(
    private val config: WiretapConfig,
    private val networkRepository: NetworkRepository,
    private val socketRepository: SocketRepository,
    private val networkLogger: NetworkLogger,
) : WiretapOrchestrator {

    // Cache of active (OPEN/CONNECTING) socket connections, used to re-create entries after log clear
    private val activeConnections = mutableMapOf<Long, SocketLogEntry>()

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

    // Socket

    override fun openSocketConnection(entry: SocketLogEntry): Long {
        if (!config.enabled) return -1
        val id = socketRepository.openConnection(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        if (config.loggingEnabled) {
            networkLogger.logSocket(entryWithId)
        }
        onSocketConnectionLogged(entryWithId)
        return id
    }

    override fun updateSocketConnection(entry: SocketLogEntry) {
        if (!config.enabled) return
        socketRepository.updateConnection(entry)
        when (entry.status) {
            dev.skymansandy.wiretap.domain.model.SocketStatus.CLOSED,
            dev.skymansandy.wiretap.domain.model.SocketStatus.FAILED -> activeConnections.remove(entry.id)
            else -> activeConnections[entry.id] = entry
        }
        if (config.loggingEnabled) {
            networkLogger.logSocket(entry)
        }
        onSocketConnectionLogged(entry)
    }

    override fun logSocketMessage(message: SocketMessage) {
        if (!config.enabled) return

        // If the socket entry was cleared but the connection is still active, re-create it
        val existingEntry = socketRepository.getById(message.socketId)
        if (existingEntry == null) {
            val cached = activeConnections[message.socketId]
            if (cached != null) {
                val reopened = cached.copy(historyCleared = true, messageCount = 0)
                socketRepository.reopenConnection(reopened)
                onSocketConnectionLogged(reopened)
            }
        }

        socketRepository.logMessage(message)
        if (config.loggingEnabled) {
            networkLogger.logSocketMessage(message)
        }
    }

    override fun getSocketById(id: Long): SocketLogEntry? = socketRepository.getById(id)

    override fun getSocketMessages(socketId: Long): Flow<List<SocketMessage>> =
        socketRepository.getMessages(socketId)

    override fun getAllSocketLogs(): Flow<List<SocketLogEntry>> = socketRepository.getAll()

    override fun getPagedSocketLogs(query: String): Flow<PagingData<SocketLogEntry>> =
        socketRepository.getPagedConnections(query)

    override fun clearSocketLogs() {
        socketRepository.clearAll()
        onSocketLogsCleared()
    }
}
