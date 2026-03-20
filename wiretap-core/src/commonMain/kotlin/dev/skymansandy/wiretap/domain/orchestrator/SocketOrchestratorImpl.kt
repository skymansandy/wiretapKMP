package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.launcher.onSocketConnectionLogged
import dev.skymansandy.wiretap.helper.launcher.onSocketLogsCleared
import dev.skymansandy.wiretap.helper.launcher.onSocketMessageLogged
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import kotlinx.coroutines.flow.Flow

internal class SocketOrchestratorImpl(
    private val socketRepository: SocketRepository,
    private val wiretapLogger: WiretapLogger,
) : SocketOrchestrator {

    // Cache of active (OPEN/CONNECTING) socket connections, used to re-create entries after log clear
    private val activeConnections = mutableMapOf<Long, SocketLogEntry>()

    override suspend fun openSocketConnection(entry: SocketLogEntry): Long {

        val id = socketRepository.openConnection(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        wiretapLogger.logSocket(entryWithId)
        onSocketConnectionLogged(entryWithId)
        return id
    }

    override suspend fun updateSocketConnection(entry: SocketLogEntry) {

        socketRepository.updateConnection(entry)
        when (entry.status) {
            dev.skymansandy.wiretap.domain.model.SocketStatus.Closed,
            dev.skymansandy.wiretap.domain.model.SocketStatus.Failed,
            -> activeConnections.remove(entry.id)
            else -> activeConnections[entry.id] = entry
        }
        wiretapLogger.logSocket(entry)
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
        wiretapLogger.logSocketMessage(message)
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
