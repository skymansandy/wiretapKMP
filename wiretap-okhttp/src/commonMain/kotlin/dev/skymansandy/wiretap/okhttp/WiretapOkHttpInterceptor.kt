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
import dev.skymansandy.wiretap.okhttp.timing.WiretapTimingRegistry
import dev.skymansandy.wiretap.okhttp.util.extractResponseMetadata
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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
@OptIn(ExperimentalAtomicApi::class)
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor, KoinComponent {

    private val config = WiretapConfig().apply(configure)

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val httpLogManager: HttpLogManager by inject()
    private val findMatchingRule: FindMatchingRuleUseCase by inject()

    /**
     * Install this on the same [okhttp3.OkHttpClient.Builder] to capture
     * granular timing (DNS, TCP, TLS, Request, Waiting, Download).
     *
     * ```kotlin
     * val wiretap = WiretapOkHttpInterceptor { ... }
     * OkHttpClient.Builder()
     *     .addInterceptor(wiretap)
     *     .eventListenerFactory(wiretap.eventListenerFactory)
     *     .build()
     * ```
     */
    val eventListenerFactory: EventListener.Factory = EventListener.Factory { call ->
        WiretapTimingRegistry.create(call)
    }

    private val sessionInitialized = AtomicBoolean(false)

    // Retention cleanup: runs once per plugin installation
    private suspend fun initSessionIfNeeded() {
        if (sessionInitialized.compareAndSet(expectedValue = false, newValue = true)) {
            when (val logRetention = config.logRetention) {
                LogRetention.Forever -> Unit
                is LogRetention.AppSession -> httpLogManager.clearHttpLogs()
                is LogRetention.Days -> {
                    val cutoff = currentTimeMillis() - logRetention.days * 24L * 60 * 60 * 1000
                    httpLogManager.purgeHttpLogsOlderThan(cutoff)
                }
            }
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {

        if (!config.enabled) {
            return@runBlocking chain.proceed(chain.request())
        }

        initSessionIfNeeded()

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

        val source = when (matchingRule?.action) {
            is RuleAction.Throttle -> ResponseSource.Throttle
            else -> ResponseSource.Network
        }

        if (logEntryId >= 0) {
            val meta = extractResponseMetadata(response, chain)
            val timingCollector = WiretapTimingRegistry.retrieve(chain.call())
            val timingPhases = timingCollector?.toTimingPhases(startNano) ?: emptyList()
            httpLogManager.updateHttp(
                HttpLog(
                    id = logEntryId,
                    url = url,
                    method = method,
                    requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                    requestBody = null,
                    responseCode = response.code,
                    responseHeaders = meta.responseHeaders.applyHeaderAction(config.headerAction),
                    responseBody = meta.responseBody,
                    durationMs = durationMs,
                    durationNs = durationNs,
                    source = source,
                    timestamp = currentTimeMillis(),
                    protocol = meta.protocol,
                    remoteAddress = meta.remoteAddress,
                    tlsProtocol = meta.tlsProtocol,
                    cipherSuite = meta.cipherSuite,
                    certificateCn = meta.certificateCn,
                    issuerCn = meta.issuerCn,
                    certificateExpiry = meta.certificateExpiry,
                    timingPhases = timingPhases,
                ),
            )
        }

        response
    }
}
