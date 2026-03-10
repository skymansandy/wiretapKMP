package dev.skymansandy.wiretap.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.db.NetworkLogEntity
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.model.HeadersSerializer
import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.model.ResponseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NetworkDaoImpl(
    private val database: WiretapDatabase,
) : NetworkDao {

    private val queries get() = database.wiretapQueries

    override fun insert(entry: NetworkLogEntry) {
        queries.insertNetworkLog(
            url = entry.url,
            method = entry.method,
            request_headers = HeadersSerializer.serialize(entry.requestHeaders),
            request_body = entry.requestBody,
            response_code = entry.responseCode.toLong(),
            response_headers = HeadersSerializer.serialize(entry.responseHeaders),
            response_body = entry.responseBody,
            duration_ms = entry.durationMs,
            source = entry.source.name,
            timestamp = entry.timestamp,
        )
    }

    override fun getAll(): Flow<List<NetworkLogEntry>> {
        return queries.getAllNetworkLogs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getById(id: Long): NetworkLogEntry? {
        return queries.getNetworkLogById(id).executeAsOneOrNull()?.toDomain()
    }

    override fun deleteAll() {
        queries.deleteAllNetworkLogs()
    }

    override fun deleteById(id: Long) {
        queries.deleteNetworkLogById(id)
    }

    private fun NetworkLogEntity.toDomain(): NetworkLogEntry {
        return NetworkLogEntry(
            id = id,
            url = url,
            method = method,
            requestHeaders = HeadersSerializer.deserialize(request_headers),
            requestBody = request_body,
            responseCode = response_code.toInt(),
            responseHeaders = HeadersSerializer.deserialize(response_headers),
            responseBody = response_body,
            durationMs = duration_ms,
            source = ResponseSource.valueOf(source),
            timestamp = timestamp,
        )
    }
}
