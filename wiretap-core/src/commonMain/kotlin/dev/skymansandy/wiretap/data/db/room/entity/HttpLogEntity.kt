package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "HttpLogEntity",
    indices = [
        Index(
            value = ["timestamp"],
            name = "idx_http_log_timestamp",
        ),
        Index(
            value = ["response_code"],
            name = "idx_http_log_response_code",
        ),
    ],
)
internal data class HttpLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "method")
    val method: String,
    @ColumnInfo(name = "request_headers", defaultValue = "")
    val requestHeaders: String = "",
    @ColumnInfo(name = "request_body")
    val requestBody: String? = null,
    @ColumnInfo(name = "response_code")
    val responseCode: Long,
    @ColumnInfo(name = "response_headers", defaultValue = "")
    val responseHeaders: String = "",
    @ColumnInfo(name = "response_body")
    val responseBody: String? = null,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "source", defaultValue = "Network")
    val source: String = "Network",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    @ColumnInfo(name = "matched_rule_id")
    val matchedRuleId: Long? = null,
    @ColumnInfo(name = "protocol")
    val protocol: String? = null,
    @ColumnInfo(name = "remote_address")
    val remoteAddress: String? = null,
    @ColumnInfo(name = "tls_protocol")
    val tlsProtocol: String? = null,
    @ColumnInfo(name = "cipher_suite")
    val cipherSuite: String? = null,
    @ColumnInfo(name = "certificate_cn")
    val certificateCn: String? = null,
    @ColumnInfo(name = "issuer_cn")
    val issuerCn: String? = null,
    @ColumnInfo(name = "certificate_expiry")
    val certificateExpiry: String? = null,
)
