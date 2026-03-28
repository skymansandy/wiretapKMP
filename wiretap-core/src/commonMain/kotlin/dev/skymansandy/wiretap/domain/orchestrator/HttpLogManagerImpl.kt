package dev.skymansandy.wiretap.domain.orchestrator

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.helper.launcher.onClearHttpLogs
import dev.skymansandy.wiretap.helper.launcher.onNewHttpLog
import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import kotlinx.coroutines.flow.Flow

internal class HttpLogManagerImpl(
    private val httpRepository: HttpRepository,
    private val wiretapLogger: WiretapLogger,
) : HttpLogManager {

    override fun flowHttpLogs(): Flow<List<HttpLog>> = httpRepository.flowAll()

    override fun flowPagedHttpLogsForSearchQuery(query: String): Flow<PagingData<HttpLog>> =
        httpRepository.flowPagesLogs(query)

    override fun flowHttpLogById(id: Long): Flow<HttpLog?> = httpRepository.flowById(id)

    override suspend fun logHttp(entry: HttpLog) {
        httpRepository.save(entry)
        wiretapLogger.logHttp(entry)
        onNewHttpLog(entry)
    }

    override suspend fun logHttpAndGetId(entry: HttpLog): Long {
        val id = httpRepository.saveAndGetId(entry)
        val entryWithId = entry.copy(id = id)
        wiretapLogger.logHttp(entryWithId)
        onNewHttpLog(entryWithId)
        return id
    }

    override suspend fun updateHttp(entry: HttpLog) {
        httpRepository.update(entry)
        wiretapLogger.logHttp(entry)
        onNewHttpLog(entry)
    }

    override suspend fun getHttpLogById(id: Long): HttpLog? = httpRepository.getById(id)

    override suspend fun deleteHttpLog(id: Long) {
        httpRepository.deleteById(id)
    }

    override suspend fun clearHttpLogs() {
        httpRepository.clearAll()
        onClearHttpLogs()
    }

    override suspend fun purgeHttpLogsOlderThan(cutoffMs: Long) {
        httpRepository.deleteOlderThan(cutoffMs)
    }

    override suspend fun markHttpCancelledIfInProgress(id: Long) {
        httpRepository.markCancelledIfInProgress(id)
    }
}
