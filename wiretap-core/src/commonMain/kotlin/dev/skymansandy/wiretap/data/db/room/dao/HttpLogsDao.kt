package dev.skymansandy.wiretap.data.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogListProjection
import kotlinx.coroutines.flow.Flow

@Suppress("LongParameterList")
@Dao
internal interface HttpLogsDao {

    @Insert
    suspend fun insert(entity: HttpLogEntity): Long

    @Query(
        """
        UPDATE HttpLogEntity SET
            response_code = :responseCode,
            response_headers = :responseHeaders,
            response_body = :responseBody,
            duration_ms = :durationMs,
            source = :source,
            matched_rule_id = :matchedRuleId,
            protocol = :protocol,
            remote_address = :remoteAddress,
            tls_protocol = :tlsProtocol,
            cipher_suite = :cipherSuite,
            certificate_cn = :certificateCn,
            issuer_cn = :issuerCn,
            certificate_expiry = :certificateExpiry,
            timing_phases = :timingPhases
        WHERE id = :id
        """,
    )
    suspend fun update(
        responseCode: Long,
        responseHeaders: String,
        responseBody: String?,
        durationMs: Long,
        source: String,
        matchedRuleId: Long?,
        protocol: String?,
        remoteAddress: String?,
        tlsProtocol: String?,
        cipherSuite: String?,
        certificateCn: String?,
        issuerCn: String?,
        certificateExpiry: String?,
        timingPhases: String?,
        id: Long,
    )

    @Query(
        """
        SELECT id, url, method, request_headers, response_code, response_headers,
            COALESCE(LENGTH(response_body), 0) AS response_body_size,
            duration_ms, source, timestamp, matched_rule_id, protocol, remote_address,
            tls_protocol, cipher_suite, certificate_cn, issuer_cn, certificate_expiry
        FROM HttpLogEntity ORDER BY timestamp DESC
        """,
    )
    fun getAllNetworkLogs(): Flow<List<HttpLogListProjection>>

    @Query("SELECT * FROM HttpLogEntity WHERE id = :id")
    suspend fun getById(id: Long): HttpLogEntity?

    @Query("SELECT * FROM HttpLogEntity WHERE id = :id")
    fun flowById(id: Long): Flow<HttpLogEntity?>

    @Query("DELETE FROM HttpLogEntity")
    suspend fun deleteAll()

    @Query("DELETE FROM HttpLogEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM HttpLogEntity WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("UPDATE HttpLogEntity SET response_code = 0 WHERE response_code = -2")
    suspend fun closeStaleHttpLogs()

    @Query("UPDATE HttpLogEntity SET response_code = -1 WHERE id = :id AND response_code = -2")
    suspend fun markCancelledIfInProgress(id: Long)

    @Query(
        """
        SELECT id, url, method, request_headers, response_code, response_headers,
            COALESCE(LENGTH(response_body), 0) AS response_body_size,
            duration_ms, source, timestamp, matched_rule_id, protocol, remote_address,
            tls_protocol, cipher_suite, certificate_cn, issuer_cn, certificate_expiry
        FROM HttpLogEntity
        WHERE (url LIKE '%' || :query || '%'
           OR method LIKE '%' || :query || '%'
           OR CAST(response_code AS TEXT) LIKE '%' || :query || '%')
        AND (:afterId IS NULL OR id < :afterId)
        ORDER BY id DESC
        LIMIT :limit
        """,
    )
    suspend fun getPage(query: String, afterId: Long?, limit: Long): List<HttpLogListProjection>
}
