package dev.skymansandy.wiretap.core.model

data class KurlResponse(
    val statusCode: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val body: String,
    val timeMs: Long,
    val sizeBytes: Long,
    val networkInfo: NetworkInfo? = null,
)

data class NetworkInfo(
    val httpVersion: String? = null,
    val localAddress: String? = null,
    val remoteAddress: String? = null,
    val tlsProtocol: String? = null,
    val cipherName: String? = null,
    val certificateCN: String? = null,
    val issuerCN: String? = null,
    val validUntil: String? = null
)
