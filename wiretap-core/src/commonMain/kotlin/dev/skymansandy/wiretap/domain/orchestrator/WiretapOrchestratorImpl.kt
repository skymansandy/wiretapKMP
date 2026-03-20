package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.launcher.onNetworkEntryLogged
import dev.skymansandy.wiretap.helper.launcher.onNetworkLogsCleared
import dev.skymansandy.wiretap.helper.launcher.onSocketConnectionLogged
import dev.skymansandy.wiretap.helper.launcher.onSocketLogsCleared
import dev.skymansandy.wiretap.helper.launcher.onSocketMessageLogged
import kotlinx.coroutines.flow.Flow

internal class WiretapOrchestratorImpl(
    private val networkRepository: NetworkRepository,
    private val socketRepository: SocketRepository,
    private val networkLogger: NetworkLogger,
) : WiretapOrchestrator {

    // Cache of active (OPEN/CONNECTING) socket connections, used to re-create entries after log clear
    private val activeConnections = mutableMapOf<Long, SocketLogEntry>()

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

    // Socket

    override suspend fun openSocketConnection(entry: SocketLogEntry): Long {

        val id = socketRepository.openConnection(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        networkLogger.logSocket(entryWithId)
        onSocketConnectionLogged(entryWithId)
        return id
    }

    override suspend fun updateSocketConnection(entry: SocketLogEntry) {

        socketRepository.updateConnection(entry)
        when (entry.status) {
            dev.skymansandy.wiretap.domain.model.SocketStatus.Closed,
            dev.skymansandy.wiretap.domain.model.SocketStatus.Failed -> activeConnections.remove(entry.id)
            else -> activeConnections[entry.id] = entry
        }
        networkLogger.logSocket(entry)
        onSocketConnectionLogged(entry)
    }

    override suspend fun logSocketMessage(message: SocketMessage) {

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
        networkLogger.logSocketMessage(message)
        socketRepository.getById(message.socketId)?.let { entry ->
            onSocketMessageLogged(entry, message)
        }
    }

    override suspend fun getSocketById(id: Long): SocketLogEntry? = socketRepository.getById(id)

    override fun getSocketByIdFlow(id: Long): Flow<SocketLogEntry?> = socketRepository.getByIdFlow(id)

    override fun getSocketMessages(socketId: Long): Flow<List<SocketMessage>> =
        socketRepository.getMessages(socketId)

    override fun getAllSocketLogs(): Flow<List<SocketLogEntry>> = socketRepository.getAll()

    override fun getPagedSocketLogs(query: String): Flow<PagingData<SocketLogEntry>> =
        socketRepository.getPagedConnections(query)

    override suspend fun clearSocketLogs() {

        socketRepository.clearAll()
        onSocketLogsCleared()
    }
}
