package dev.skymansandy.wiretap.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.db.NetworkLogEntity
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.util.HeadersSerializerUtil
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

    override fun insertAndGetId(entry: NetworkLogEntry): Long {
        return database.transactionWithResult {
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

    override fun update(entry: NetworkLogEntry) {
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

    override fun getAll(): Flow<List<NetworkLogEntry>> {
        return queries.getAllNetworkLogs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getPage(query: String, limit: Long, afterId: Long?): List<NetworkLogEntry> {
        return queries.getNetworkLogsPage(query, afterId, limit)
            .executeAsList()
            .map { it.toDomain() }
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
            requestHeaders = HeadersSerializerUtil.deserialize(request_headers),
            requestBody = request_body,
            responseCode = response_code.toInt(),
            responseHeaders = HeadersSerializerUtil.deserialize(response_headers),
            responseBody = response_body,
            durationMs = duration_ms,
            source = ResponseSource.valueOf(source),
            timestamp = timestamp,
            matchedRuleId = matched_rule_id,
            protocol = protocol,
            remoteAddress = remote_address,
            tlsProtocol = tls_protocol,
            cipherSuite = cipher_suite,
            certificateCn = certificate_cn,
            issuerCn = issuer_cn,
            certificateExpiry = certificate_expiry,
        )
    }
}
