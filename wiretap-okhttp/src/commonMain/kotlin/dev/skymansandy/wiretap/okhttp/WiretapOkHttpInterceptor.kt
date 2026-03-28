package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.config.LogRetention
import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import dev.skymansandy.wiretap.domain.model.config.applyHeaderAction
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.helper.util.currentNanoTime
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
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

    private val httpLogManager: HttpLogManager by inject()
    private val findMatchingRule: FindMatchingRuleUseCase by inject()

    @Volatile
    private var sessionInitialized = false
    private val lock = Mutex()

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {

        if (!config.enabled) {
            return@runBlocking chain.proceed(chain.request())
        }

        // AppSession: clear all previous logs once at first interception
        if (!sessionInitialized) {
            lock.withLock {
                if (!sessionInitialized) {
                    sessionInitialized = true
                    when (val retention = config.logRetention) {
                        LogRetention.Forever -> Unit
                        LogRetention.AppSession -> httpLogManager.clearHttpLogs()
                        is LogRetention.Days -> {
                            val cutoff = currentTimeMillis() - retention.days * 24L * 60 * 60 * 1000
                            httpLogManager.purgeHttpLogsOlderThan(cutoff)
                        }
                    }
                }
            }
        }

        val request = chain.request()

        // Skip WebSocket upgrade requests — handled by WiretapOkHttpWebSocketListener
        if (request.header("Upgrade").equals("websocket", ignoreCase = true)) {
            return@runBlocking chain.proceed(request)
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

        val matchingRule = findMatchingRule(url, method, reqHeaders, requestBody)

        // Log request immediately so it appears in the UI (gated by shouldLog).
        // NonCancellable ensures the save completes and returns the ID even if the
        // coroutine is canceled mid-flight, preventing orphaned "..." entries.
        val logEntryId = if (config.shouldLog(url, method)) {
            withContext(NonCancellable) {
                httpLogManager.logHttpAndGetId(
                    HttpLog(
                        url = url,
                        method = method,
                        requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        } else {
            -1L
        }

        if (matchingRule?.action is RuleAction.Mock) {
            val mock = matchingRule.action as RuleAction.Mock
            val body = (mock.responseBody ?: "")
                .toResponseBody("application/json; charset=utf-8".toMediaType())
            val mockResponse = Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(mock.responseCode)
                .message("Mock")
                .body(body)
                .apply { mock.responseHeaders?.forEach { (k, v) -> addHeader(k, v) } }
                .build()

            if (logEntryId >= 0) {
                val mockRespHeaders = mockResponse.headers.toMap()
                val durationNs = currentNanoTime() - startNano
                httpLogManager.updateHttp(
                    HttpLog(
                        id = logEntryId,
                        url = url,
                        method = method,
                        requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
                        responseCode = mockResponse.code,
                        responseHeaders = mockRespHeaders.applyHeaderAction(config.headerAction),
                        responseBody = mock.responseBody,
                        durationMs = durationNs / 1_000_000,
                        durationNs = durationNs,
                        source = ResponseSource.Mock,
                        timestamp = currentTimeMillis(),
                    ),
                )
            }

            return@runBlocking mockResponse
        }

        if (matchingRule?.action is RuleAction.Throttle) {
            val throttle = matchingRule.action as RuleAction.Throttle
            val minDelay = throttle.delayMs
            val maxDelay = throttle.delayMaxMs ?: minDelay
            val delayMs = if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
            if (delayMs > 0) Thread.sleep(delayMs)
        }

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            if (logEntryId >= 0) {
                val isCancelled = e is IOException && e.message == "Canceled"
                withContext(NonCancellable) {
                    val durationNs = currentNanoTime() - startNano
                    httpLogManager.updateHttp(
                        HttpLog(
                            id = logEntryId,
                            url = url,
                            method = method,
                            requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                            requestBody = requestBody,
                            responseCode = if (isCancelled) -1 else 0,
                            responseHeaders = emptyMap(),
                            responseBody = e.message ?: e::class.simpleName ?: "Unknown error",
                            durationMs = durationNs / 1_000_000,
                            durationNs = durationNs,
                            source = ResponseSource.Network,
                            timestamp = currentTimeMillis(),
                        ),
                    )
                }
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
            is RuleAction.Throttle -> ResponseSource.Throttle
            else -> ResponseSource.Network
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
            httpLogManager.updateHttp(
                HttpLog(
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

        response
    }
}
