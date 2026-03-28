package dev.skymansandy.wiretap.okhttp.util

import okhttp3.Interceptor
import okhttp3.Response
import java.security.cert.X509Certificate

internal data class ResponseMetadata(
    val responseHeaders: Map<String, String>,
    val responseBody: String?,
    val protocol: String,
    val remoteAddress: String?,
    val tlsProtocol: String?,
    val cipherSuite: String?,
    val certificateCn: String?,
    val issuerCn: String?,
    val certificateExpiry: String?,
)

internal fun extractResponseMetadata(
    response: Response,
    chain: Interceptor.Chain,
): ResponseMetadata {

    val responseHeaders = response.headers.toMap()
    val responseBody = try {
        response.peekBody(Long.MAX_VALUE).string()
    } catch (_: Exception) {
        null
    }

    val protocol = response.protocol.toString()
    val remoteAddress = try {
        chain.connection()?.route()?.socketAddress?.let { "${it.hostName}:${it.port}" }
    } catch (_: Exception) {
        null
    }

    val handshake = response.handshake
    val tlsProtocol = handshake?.tlsVersion?.javaName
    val cipherSuite = handshake?.cipherSuite?.javaName
    val peerCert = try {
        handshake?.peerCertificates?.firstOrNull() as? X509Certificate
    } catch (_: Exception) {
        null
    }

    val certificateCn = peerCert?.extractCn { subjectX500Principal }
    val issuerCn = peerCert?.extractCn { issuerX500Principal }
    val certificateExpiry = peerCert?.notAfter?.toString()

    return ResponseMetadata(
        responseHeaders = responseHeaders,
        responseBody = responseBody,
        protocol = protocol,
        remoteAddress = remoteAddress,
        tlsProtocol = tlsProtocol,
        cipherSuite = cipherSuite,
        certificateCn = certificateCn,
        issuerCn = issuerCn,
        certificateExpiry = certificateExpiry,
    )
}

private fun X509Certificate.extractCn(
    principal: X509Certificate.() -> javax.security.auth.x500.X500Principal,
): String? = principal().name
    ?.split(",")
    ?.firstOrNull { it.trimStart().startsWith("CN=") }
    ?.substringAfter("CN=")
    ?.trim()
