package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.ResponseSource

data class NetworkLogEntry(
    val id: Long = 0,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val durationMs: Long,
    val durationNs: Long = 0,
    val source: ResponseSource = ResponseSource.NETWORK,
    val timestamp: Long,
    val matchedRuleId: Long? = null,
)
