package dev.skymansandy.wiretap.data.db.room.entity

import androidx.room.ColumnInfo

internal data class HttpLogListProjection(
    val id: Long,
    val url: String,
    val method: String,
    @ColumnInfo(name = "request_headers")
    val requestHeaders: String,
    @ColumnInfo(name = "response_code")
    val responseCode: Long,
    @ColumnInfo(name = "response_headers")
    val responseHeaders: String,
    @ColumnInfo(name = "response_body_size")
    val responseBodySize: Long,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    val source: String,
    val timestamp: Long,
    @ColumnInfo(name = "matched_rule_id")
    val matchedRuleId: Long?,
    val protocol: String?,
    @ColumnInfo(name = "remote_address")
    val remoteAddress: String?,
    @ColumnInfo(name = "tls_protocol")
    val tlsProtocol: String?,
    @ColumnInfo(name = "cipher_suite")
    val cipherSuite: String?,
    @ColumnInfo(name = "certificate_cn")
    val certificateCn: String?,
    @ColumnInfo(name = "issuer_cn")
    val issuerCn: String?,
    @ColumnInfo(name = "certificate_expiry")
    val certificateExpiry: String?,
)
