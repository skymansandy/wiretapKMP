package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.config.applyHeaderAction
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.util.currentNanoTime
import dev.skymansandy.wiretap.util.currentTimeMillis
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.security.cert.X509Certificate

/**
 * OkHttp interceptor for Wiretap network inspection.
 *
 * Configuration is applied via a builder lambda:
 * ```kotlin
 * OkHttpClient.Builder()
 *     .addInterceptor(WiretapOkHttpInterceptor {
 *         shouldLog = { url, _ -> url.contains("/api/") }
 *         headerAction = { key ->
 *             if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
 *             else HeaderAction.Keep
 *         }
 *         logRetention = LogRetention.Days(7)
 *     })
 *     .build()
 * ```
 */
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor, KoinComponent {

    private val config = WiretapConfig().apply(configure)

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val orchestrator: WiretapOrchestrator by inject()
    private val ruleRepository: RuleRepository by inject()

    @Volatile private var sessionInitialized = false

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!config.enabled) return chain.proceed(chain.request())

        // AppSession: clear all previous logs once at first interception
        if (!sessionInitialized) {
            synchronized(this) {
                if (!sessionInitialized) {
                    sessionInitialized = true
                    if (config.logRetention == LogRetention.AppSession) {
                        orchestrator.clearLogs()
                    }
                }
            }
        }

        val request = chain.request()

        // Skip WebSocket upgrade requests — handled by WiretapOkHttpWebSocketListener
        if (request.header("Upgrade").equals("websocket", ignoreCase = true)) {
            return chain.proceed(request)
        }

        val url = request.url.toString()
        val method = request.method
        val startNano = currentNanoTime()

        val reqHeaders = request.headers.toMap()
        val requestBody = try {
            val copy = request.newBuilder().build()
            val buffer = okio.Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (_: Exception) {
            null
        }

        val matchingRule = ruleRepository.findMatchingRule(url, method, reqHeaders, requestBody)

        // Days retention: prune old entries before each new capture
        val retention = config.logRetention
        if (retention is LogRetention.Days) {
            val cutoff = currentTimeMillis() - retention.days * 24L * 60 * 60 * 1000
            orchestrator.purgeLogsOlderThan(cutoff)
        }

        // Log request immediately so it appears in the UI (gated by shouldLog)
        val logEntryId = if (config.shouldLog(url, method)) {
            orchestrator.logRequest(
                NetworkLogEntry(
                    url = url,
                    method = method,
                    requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                    requestBody = requestBody,
                    timestamp = currentTimeMillis(),
                ),
            )
        } else {
            -1L
        }

        if (matchingRule?.action == RuleAction.MOCK) {
            val durationNs = currentNanoTime() - startNano
            val body = (matchingRule.mockResponseBody ?: "")
                .toResponseBody("application/json; charset=utf-8".toMediaType())
            val mockResponse = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(matchingRule.mockResponseCode ?: 200)
                .message("Mock")
                .body(body)
                .apply { matchingRule.mockResponseHeaders?.forEach { (k, v) -> addHeader(k, v) } }
                .build()

            if (logEntryId >= 0) {
                val mockRespHeaders = mockResponse.headers.toMap()
                orchestrator.updateEntry(
                    NetworkLogEntry(
                        id = logEntryId,
                        url = url,
                        method = method,
                        requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
                        responseCode = mockResponse.code,
                        responseHeaders = mockRespHeaders.applyHeaderAction(config.headerAction),
                        responseBody = matchingRule.mockResponseBody,
                        durationMs = durationNs / 1_000_000,
                        durationNs = durationNs,
                        source = ResponseSource.MOCK,
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            return mockResponse
        }

        if (matchingRule?.action == RuleAction.THROTTLE) {
            val minDelay = matchingRule.throttleDelayMs ?: 0L
            val maxDelay = matchingRule.throttleDelayMaxMs ?: minDelay
            val delayMs = if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
            if (delayMs > 0) Thread.sleep(delayMs)
        }

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            if (logEntryId >= 0) {
                val durationNs = currentNanoTime() - startNano
                val isCancelled = e is java.io.IOException && e.message == "Canceled"
                orchestrator.updateEntry(
                    NetworkLogEntry(
                        id = logEntryId,
                        url = url,
                        method = method,
                        requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
                        responseCode = if (isCancelled) -1 else 0,
                        responseHeaders = emptyMap(),
                        responseBody = e.message ?: e::class.simpleName ?: "Unknown error",
                        durationMs = (currentNanoTime() - startNano) / 1_000_000,
                        durationNs = currentNanoTime() - startNano,
                        source = ResponseSource.NETWORK,
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            throw e
        }

        val durationNs = currentNanoTime() - startNano
        val durationMs = durationNs / 1_000_000

        val responseHeaders = response.headers.toMap()
        val responseBody = try {
            response.peekBody(Long.MAX_VALUE).string()
        } catch (_: Exception) {
            null
        }

        val source = when (matchingRule?.action) {
            RuleAction.THROTTLE -> ResponseSource.THROTTLE
            else -> ResponseSource.NETWORK
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
        val certificateCn = peerCert?.subjectX500Principal?.name
            ?.split(",")
            ?.firstOrNull { it.trimStart().startsWith("CN=") }
            ?.substringAfter("CN=")
            ?.trim()
        val issuerCn = peerCert?.issuerX500Principal?.name
            ?.split(",")
            ?.firstOrNull { it.trimStart().startsWith("CN=") }
            ?.substringAfter("CN=")
            ?.trim()
        val certificateExpiry = peerCert?.notAfter?.toString()

        if (logEntryId >= 0) {
            orchestrator.updateEntry(
                NetworkLogEntry(
                    id = logEntryId,
                    url = url,
                    method = method,
                    requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                    requestBody = null,
                    responseCode = response.code,
                    responseHeaders = responseHeaders.applyHeaderAction(config.headerAction),
                    responseBody = responseBody,
                    durationMs = durationMs,
                    durationNs = durationNs,
                    source = source,
                    timestamp = currentTimeMillis(),
                    protocol = protocol,
                    remoteAddress = remoteAddress,
                    tlsProtocol = tlsProtocol,
                    cipherSuite = cipherSuite,
                    certificateCn = certificateCn,
                    issuerCn = issuerCn,
                    certificateExpiry = certificateExpiry,
                ),
            )
        }

        return response
    }
}
