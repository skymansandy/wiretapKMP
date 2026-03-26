package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
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
    private val activeConnections = mutableMapOf<Long, SocketEntry>()

    override suspend fun createSocket(entry: SocketEntry): Long {
        val id = socketRepository.openConnection(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        wiretapLogger.logSocket(entryWithId)
        onSocketConnectionLogged(entryWithId)
        return id
    }

    override suspend fun updateSocket(entry: SocketEntry) {
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

    override suspend fun logSocketMsg(message: SocketMessage) {
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

    override suspend fun getSocketById(id: Long): SocketEntry? = socketRepository.getById(id)

    override fun flowSocketById(id: Long): Flow<SocketEntry?> = socketRepository.getByIdFlow(id)

    override fun flowSocketMessagesById(socketId: Long): Flow<List<SocketMessage>> =
        socketRepository.getMessages(socketId)

    override fun getAllSockets(): Flow<List<SocketEntry>> = socketRepository.getAll()

    override fun getPagedSockets(query: String): Flow<PagingData<SocketEntry>> =
        socketRepository.getPagedConnections(query)

    override suspend fun clearLogs() {
        socketRepository.clearAll()
        onSocketLogsCleared()
    }
}
