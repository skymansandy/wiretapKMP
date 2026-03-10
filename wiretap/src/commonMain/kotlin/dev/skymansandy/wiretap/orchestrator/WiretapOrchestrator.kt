package dev.skymansandy.wiretap.orchestrator

import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.flow.Flow

interface WiretapOrchestrator {
    fun logEntry(entry: NetworkLogEntry)
    fun getAllLogs(): Flow<List<NetworkLogEntry>>
    fun getLogById(id: Long): NetworkLogEntry?
    fun clearLogs()
}
