package dev.skymansandy.spektorsample.core

import java.net.Socket
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509ExtendedTrustManager
import javax.net.ssl.X509TrustManager

internal data class SslCapture(
    val protocol: String?,
    val cipher: String?,
    val chain: Array<X509Certificate>?
)

internal val lastSslCapture = AtomicReference<SslCapture?>(null)

internal fun createCapturingSslContext(): SSLContext {
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(null as KeyStore?)
    val delegate = tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
    val context = SSLContext.getInstance("TLS")
    context.init(null, arrayOf(CapturingTrustManager(delegate)), null)
    return context
}

private class CapturingTrustManager(
    private val delegate: X509TrustManager
) : X509ExtendedTrustManager() {

    // Called by Java's HttpClient (uses SSLEngine, not SSLSocket)
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine) {
        delegate.checkServerTrusted(chain, authType)
        val session = engine.handshakeSession
        lastSslCapture.set(
            SslCapture(
                protocol = session?.protocol,
                cipher = session?.cipherSuite,
                chain = chain.map { it as X509Certificate }.toTypedArray()
            )
        )
    }

    // Called by legacy socket-based HTTPS (Android engine, OkHttp, etc.)
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, socket: Socket) {
        delegate.checkServerTrusted(chain, authType)
        if (socket is javax.net.ssl.SSLSocket) {
            val session = socket.session
            lastSslCapture.set(
                SslCapture(
                    protocol = session?.protocol,
                    cipher = session?.cipherSuite,
                    chain = chain.map { it as X509Certificate }.toTypedArray()
                )
            )
        }
    }

    // Fallback — called when neither engine nor socket is available
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        delegate.checkServerTrusted(chain, authType)
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {}
    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String, socket: Socket) {}
    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> =
        delegate.acceptedIssuers.map { it as X509Certificate }.toTypedArray()
}