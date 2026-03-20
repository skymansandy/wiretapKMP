package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.ResponseSource

data class HttpLogEntry(
    val id: Long = 0,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int = RESPONSE_CODE_IN_PROGRESS,
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val durationMs: Long = 0,
    val durationNs: Long = 0,
    val source: ResponseSource = ResponseSource.Network,
    val timestamp: Long,
    val matchedRuleId: Long? = null,
    val protocol: String? = null,
    val remoteAddress: String? = null,
    val tlsProtocol: String? = null,
    val cipherSuite: String? = null,
    val certificateCn: String? = null,
    val issuerCn: String? = null,
    val certificateExpiry: String? = null,
) {
    val isInProgress: Boolean get() = responseCode == RESPONSE_CODE_IN_PROGRESS

    companion object {

        const val RESPONSE_CODE_IN_PROGRESS = -2
    }
}
