/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import dev.skymansandy.wiretap.helper.launcher.onClearSocketLogs
import dev.skymansandy.wiretap.helper.launcher.onNewSocketConnection
import dev.skymansandy.wiretap.helper.launcher.onNewSocketMessage
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import kotlinx.coroutines.flow.Flow

internal class SocketLogManagerImpl(
    private val socketRepository: SocketRepository,
    private val wiretapLogger: WiretapLogger,
) : SocketLogManager {

    // Cache of active (OPEN/CONNECTING) socket connections, used to re-create entries after log clear
    private val activeConnections = mutableMapOf<Long, SocketConnection>()

    override suspend fun createSocket(entry: SocketConnection): Long {
        val id = socketRepository.logNew(entry)
        val entryWithId = entry.copy(id = id)
        activeConnections[id] = entryWithId
        wiretapLogger.logSocket(entryWithId)
        onNewSocketConnection(entryWithId)
        return id
    }

    override suspend fun updateSocket(entry: SocketConnection) {
        socketRepository.update(entry)
        when (entry.status) {
            SocketStatus.Closed,
            SocketStatus.Failed,
            -> activeConnections.remove(entry.id)

            else -> activeConnections[entry.id] = entry
        }
        wiretapLogger.logSocket(entry)
        onNewSocketConnection(entry)
    }

    override suspend fun logSocketMsg(message: SocketMessage) {
        // If the socket entry was cleared but the connection is still active, re-create it
        val existingEntry = socketRepository.getById(message.socketId)
        if (existingEntry == null) {
            val cached = activeConnections[message.socketId]
            if (cached != null) {
                val reopened = cached.copy(historyCleared = true, messageCount = 0)
                socketRepository.markReopened(reopened)
                onNewSocketConnection(reopened)
            }
        }

        socketRepository.logSocketMsg(message)
        wiretapLogger.logSocketMessage(message)
        socketRepository.getById(message.socketId)?.let { entry ->
            onNewSocketMessage(entry, message)
        }
    }

    override suspend fun getSocketById(id: Long): SocketConnection? = socketRepository.getById(id)

    override fun flowSocketById(id: Long): Flow<SocketConnection?> = socketRepository.flowById(id)

    override fun flowSocketMessagesById(socketId: Long): Flow<List<SocketMessage>> =
        socketRepository.flowMessagesForId(socketId)

    override fun flowAllSockets(): Flow<List<SocketConnection>> = socketRepository.flowAll()

    override fun flowPagedSocketsForSearchQuery(query: String): Flow<PagingData<SocketConnection>> =
        socketRepository.flowForSearchQuery(query)

    override suspend fun clearLogs() {
        socketRepository.clearAll()
        onClearSocketLogs()
    }
}
