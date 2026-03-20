package dev.skymansandy.wiretap.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.helper.util.HeadersSerializerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class HttpDaoImpl(
    private val database: WiretapDatabase,
) : HttpDao {

    private val queries get() = database.wiretapQueries

    override suspend fun insert(entry: HttpLogEntry) {
        withContext(Dispatchers.IO) {
            queries.insertNetworkLog(
                url = entry.url,
                method = entry.method,
                request_headers = HeadersSerializerUtil.serialize(entry.requestHeaders),
                request_body = entry.requestBody,
                response_code = entry.responseCode.toLong(),
                response_headers = HeadersSerializerUtil.serialize(entry.responseHeaders),
                response_body = entry.responseBody,
                duration_ms = entry.durationMs,
                source = entry.source.name,
                timestamp = entry.timestamp,
                matched_rule_id = entry.matchedRuleId,
                protocol = entry.protocol,
                remote_address = entry.remoteAddress,
                tls_protocol = entry.tlsProtocol,
                cipher_suite = entry.cipherSuite,
                certificate_cn = entry.certificateCn,
                issuer_cn = entry.issuerCn,
                certificate_expiry = entry.certificateExpiry,
            )
        }
    }

    override suspend fun insertAndGetId(entry: HttpLogEntry): Long =
        withContext(Dispatchers.IO) {
            database.transactionWithResult {
                queries.insertNetworkLog(
                    url = entry.url,
                    method = entry.method,
                    request_headers = HeadersSerializerUtil.serialize(entry.requestHeaders),
                    request_body = entry.requestBody,
                    response_code = entry.responseCode.toLong(),
                    response_headers = HeadersSerializerUtil.serialize(entry.responseHeaders),
                    response_body = entry.responseBody,
                    duration_ms = entry.durationMs,
                    source = entry.source.name,
                    timestamp = entry.timestamp,
                    matched_rule_id = entry.matchedRuleId,
                    protocol = entry.protocol,
                    remote_address = entry.remoteAddress,
                    tls_protocol = entry.tlsProtocol,
                    cipher_suite = entry.cipherSuite,
                    certificate_cn = entry.certificateCn,
                    issuer_cn = entry.issuerCn,
                    certificate_expiry = entry.certificateExpiry,
                )
                queries.lastInsertRowId().executeAsOne()
            }
        }

    override suspend fun update(entry: HttpLogEntry) {
        withContext(Dispatchers.IO) {
            queries.updateNetworkLog(
                response_code = entry.responseCode.toLong(),
                response_headers = HeadersSerializerUtil.serialize(entry.responseHeaders),
                response_body = entry.responseBody,
                duration_ms = entry.durationMs,
                source = entry.source.name,
                matched_rule_id = entry.matchedRuleId,
                protocol = entry.protocol,
                remote_address = entry.remoteAddress,
                tls_protocol = entry.tlsProtocol,
                cipher_suite = entry.cipherSuite,
                certificate_cn = entry.certificateCn,
                issuer_cn = entry.issuerCn,
                certificate_expiry = entry.certificateExpiry,
                id = entry.id,
            )
        }
    }

    override fun getAll(): Flow<List<HttpLogEntry>> {
        return queries.getAllNetworkLogs()
            .asFlow()
            .flowOn(Dispatchers.IO)
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getPage(
        query: String,
        limit: Long,
        afterId: Long?,
    ): List<HttpLogEntry> = withContext(Dispatchers.IO) {
        queries.getNetworkLogsPage(query, afterId, limit)
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun getById(id: Long): HttpLogEntry? = withContext(Dispatchers.IO) {

        queries.getNetworkLogById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            queries.deleteAllNetworkLogs()
        }
    }

    override suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteNetworkLogById(id)
        }
    }

    override suspend fun deleteOlderThan(timestamp: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteNetworkLogsOlderThan(timestamp)
        }
    }
}
