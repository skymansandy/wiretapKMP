package dev.skymansandy.wiretap.core

import dev.skymansandy.wiretap.core.model.NetworkInfo

internal actual fun buildNetworkInfo(requestUrl: String, httpVersion: String): NetworkInfo {
    val isHttps = requestUrl.startsWith("https://", ignoreCase = true)
    val host = requestUrl
        .removePrefix("https://").removePrefix("http://")
        .substringBefore("/").substringBefore("?")

    // Atomically consume the last captured SSL session (null if connection was reused from pool)
    val ssl = if (isHttps) lastSslCapture.getAndSet(null) else null

    val cert = ssl?.chain?.firstOrNull()
    val cn = cert?.subjectX500Principal?.name?.cnValue()
    val issuer = cert?.issuerX500Principal?.name?.cnValue()
    val validUntil = cert?.notAfter?.toString()

    return NetworkInfo(
        httpVersion = httpVersion,
        remoteAddress = host,
        tlsProtocol = ssl?.protocol ?: if (isHttps) "TLS" else null,
        cipherName = ssl?.cipher,
        certificateCN = cn,
        issuerCN = issuer,
        validUntil = validUntil
    )
}

/** Extracts the CN= value from an X.500 principal name string. */
private fun String.cnValue(): String? =
    split(",")
        .map { it.trim() }
        .firstOrNull { it.startsWith("CN=", ignoreCase = true) }
        ?.removePrefix("CN=")
        ?.removePrefix("cn=")