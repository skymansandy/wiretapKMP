package dev.skymansandy.wiretap.urlsession

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.config.LogRetention
import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import dev.skymansandy.wiretap.domain.model.config.applyHeaderAction
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.helper.util.currentNanoTime
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCancelled
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.allHTTPHeaderFields
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_time
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Low-level URLSession interceptor for Wiretap network inspection on iOS.
 *
 * **Prefer [WiretapURLSession]** for new code — it wraps this class and manages the
 * underlying NSURLSession for you, giving a drop-in URLSession-like API.
 *
 * Use this class directly only when you need to provide your own NSURLSession
 * instance (e.g. custom configuration, delegate, or shared session).
 *
 * Provides two APIs:
 * - [intercept]: Fire-and-forget execution with full mock/throttle rule support.
 * - [dataTask]: Returns an NSURLSessionDataTask with logging (no mock/throttle).
 */
@OptIn(ExperimentalAtomicApi::class, ExperimentalForeignApi::class, BetaInteropApi::class)
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
) : KoinComponent {

    private val config = WiretapConfig().apply(configure)

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val httpLogManager: HttpLogManager by inject()
    private val findMatchingRule: FindMatchingRuleUseCase by inject()

    private val sessionInitialized = AtomicBoolean(false)

    /**
     * Intercepts a URL request with full rule support (mock, throttle).
     * Automatically executes the request and calls [completionHandler] with the result.
     * For mock rules, the completion handler is called with mock data without network access.
     */
    @Suppress("LongMethod")
    fun intercept(
        request: NSURLRequest,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) = runBlocking {
        if (!config.enabled) {
            session.dataTaskWithRequest(request) { data, response, error ->
                completionHandler(data, response as? NSHTTPURLResponse, error)
            }.resume()
            return@runBlocking
        }

        initSessionIfNeeded()

        val url = request.URL?.absoluteString ?: ""
        val method = request.HTTPMethod ?: "GET"
        val startNano = currentNanoTime()

        val reqHeaders = extractRequestHeaders(request)
        val requestBody = request.HTTPBody?.toKotlinString()

        val matchingRule = findMatchingRule(url, method, reqHeaders, requestBody)

        val logEntryId = if (config.shouldLog(url, method)) {
            httpLogManager.logHttpAndGetId(
                HttpLog(
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

        when (matchingRule?.action) {
            is RuleAction.Mock -> {
                handleMockResponse(
                    logEntryId, url, method, reqHeaders, requestBody,
                    matchingRule, startNano, ResponseSource.Mock, completionHandler,
                )
            }
            is RuleAction.MockAndThrottle -> {
                val action = matchingRule.action as RuleAction.MockAndThrottle
                val delayMs = calculateDelayMs(action.delayMs, action.delayMaxMs)
                dispatchWithDelay(delayMs) {
                    runBlocking {
                        handleMockResponse(
                            logEntryId, url, method, reqHeaders, requestBody,
                            matchingRule, startNano, ResponseSource.MockAndThrottle,
                            completionHandler,
                        )
                    }
                }
            }
            is RuleAction.Throttle -> {
                val throttle = matchingRule.action as RuleAction.Throttle
                val delayMs = calculateDelayMs(throttle.delayMs, throttle.delayMaxMs)
                dispatchWithDelay(delayMs) {
                    executeNetworkRequest(
                        request, logEntryId, url, method, reqHeaders,
                        requestBody, startNano, matchingRule, completionHandler,
                    )
                }
            }
            else -> {
                executeNetworkRequest(
                    request, logEntryId, url, method, reqHeaders,
                    requestBody, startNano, matchingRule, completionHandler,
                )
            }
        }
    }

    /**
     * Creates a data task with Wiretap logging. Returns the task — the caller must call resume().
     * Note: Mock/throttle rules are NOT applied. Use [intercept] for full rule support.
     */
    fun dataTask(
        request: NSURLRequest,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask = runBlocking {
        if (!config.enabled) {
            return@runBlocking session.dataTaskWithRequest(request, completionHandler)
        }

        initSessionIfNeeded()

        val url = request.URL?.absoluteString ?: ""
        val method = request.HTTPMethod ?: "GET"
        val startNano = currentNanoTime()

        val reqHeaders = extractRequestHeaders(request)
        val requestBody = request.HTTPBody?.toKotlinString()

        val logEntryId = if (config.shouldLog(url, method)) {
            httpLogManager.logHttpAndGetId(
                HttpLog(
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

        session.dataTaskWithRequest(request) { data, response, error ->
            val durationNs = currentNanoTime() - startNano
            val httpResponse = response as? NSHTTPURLResponse

            if (logEntryId >= 0) {
                runBlocking {
                    if (error?.code == NSURLErrorCancelled) {
                        httpLogManager.markHttpCancelledIfInProgress(logEntryId)
                    } else {
                        logResponse(
                            logEntryId, url, method, reqHeaders, requestBody,
                            httpResponse, data, error, durationNs, null,
                        )
                    }
                }
            }

            completionHandler(data, response, error)
        }
    }

    /**
     * Convenience: creates a data task for a URL string with Wiretap logging.
     */
    fun dataTask(
        url: String,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask {
        val nsUrl = NSURL.URLWithString(url)!!
        val request = NSURLRequest.requestWithURL(nsUrl)
        return dataTask(request, completionHandler)
    }

    @Suppress("LongParameterList")
    private fun executeNetworkRequest(
        request: NSURLRequest,
        logEntryId: Long,
        url: String,
        method: String,
        reqHeaders: Map<String, String>,
        requestBody: String?,
        startNano: Long,
        matchingRule: WiretapRule?,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) {
        session.dataTaskWithRequest(request) { data, response, error ->
            val durationNs = currentNanoTime() - startNano
            val httpResponse = response as? NSHTTPURLResponse
            if (logEntryId >= 0) {
                runBlocking {
                    if (error?.code == NSURLErrorCancelled) {
                        httpLogManager.markHttpCancelledIfInProgress(logEntryId)
                    } else {
                        logResponse(
                            logEntryId, url, method, reqHeaders, requestBody,
                            httpResponse, data, error, durationNs, matchingRule,
                        )
                    }
                }
            }
            completionHandler(data, httpResponse, error)
        }.resume()
    }

    private fun calculateDelayMs(minDelay: Long, maxDelay: Long?): Long {
        val max = maxDelay ?: minDelay
        return if (max > minDelay) (minDelay..max).random() else minDelay
    }

    private fun dispatchWithDelay(delayMs: Long, block: () -> Unit) {
        if (delayMs > 0) {
            val delayNs = delayMs * 1_000_000
            dispatch_after(
                dispatch_time(DISPATCH_TIME_NOW, delayNs),
                dispatch_get_global_queue(0.toLong(), 0u),
            ) { block() }
        } else {
            block()
        }
    }

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

    @Suppress("LongParameterList")
    @OptIn(BetaInteropApi::class)
    private suspend fun handleMockResponse(
        logEntryId: Long,
        url: String,
        method: String,
        reqHeaders: Map<String, String>,
        requestBody: String?,
        matchingRule: WiretapRule,
        startNano: Long,
        source: ResponseSource,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) {
        val durationNs = currentNanoTime() - startNano
        val mockCode: Int
        val mockHeaders: Map<String, String>
        val mockBody: String?

        when (val action = matchingRule.action) {
            is RuleAction.Mock -> {
                mockCode = action.responseCode
                mockHeaders = action.responseHeaders ?: emptyMap()
                mockBody = action.responseBody
            }

            is RuleAction.MockAndThrottle -> {
                mockCode = action.responseCode
                mockHeaders = action.responseHeaders ?: emptyMap()
                mockBody = action.responseBody
            }

            else -> return
        }

        if (logEntryId >= 0) {
            httpLogManager.updateHttp(
                HttpLog(
                    id = logEntryId,
                    url = url,
                    method = method,
                    requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                    requestBody = requestBody,
                    responseCode = mockCode,
                    responseHeaders = mockHeaders.applyHeaderAction(config.headerAction),
                    responseBody = mockBody,
                    durationMs = durationNs / 1_000_000,
                    durationNs = durationNs,
                    source = source,
                    timestamp = currentTimeMillis(),
                    matchedRuleId = matchingRule.id,
                ),
            )
        }

        val mockData = mockBody?.let { it.encodeToByteArray().toNSData() }
        val nsUrl = NSURL.URLWithString(url)!!

        @Suppress("UNCHECKED_CAST")
        val mockResponse = NSHTTPURLResponse(
            nsUrl,
            mockCode.toLong(),
            "HTTP/1.1",
            mockHeaders as Map<Any?, *>,
        )
        completionHandler(mockData, mockResponse, null)
    }

    @Suppress("LongParameterList")
    private suspend fun logResponse(
        logEntryId: Long,
        url: String,
        method: String,
        reqHeaders: Map<String, String>,
        requestBody: String?,
        httpResponse: NSHTTPURLResponse?,
        data: NSData?,
        error: NSError?,
        durationNs: Long,
        matchingRule: WiretapRule?,
    ) {
        val responseCode = httpResponse?.statusCode?.toInt()
            ?: if (error != null) 0 else HttpLog.RESPONSE_CODE_IN_PROGRESS

        val responseHeaders = extractResponseHeaders(httpResponse)
        val responseBody = if (error != null) {
            error.localizedDescription
        } else {
            data?.toKotlinString()
        }

        val source = when (matchingRule?.action) {
            is RuleAction.Throttle -> ResponseSource.Throttle
            else -> ResponseSource.Network
        }

        httpLogManager.updateHttp(
            HttpLog(
                id = logEntryId,
                url = url,
                method = method,
                requestHeaders = reqHeaders.applyHeaderAction(config.headerAction),
                requestBody = requestBody,
                responseCode = responseCode,
                responseHeaders = responseHeaders.applyHeaderAction(config.headerAction),
                responseBody = responseBody,
                durationMs = durationNs / 1_000_000,
                durationNs = durationNs,
                source = source,
                timestamp = currentTimeMillis(),
                matchedRuleId = matchingRule?.id,
            ),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractRequestHeaders(request: NSURLRequest): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        (request.allHTTPHeaderFields as? Map<String, String>)?.forEach { (key, value) ->
            headers[key] = value
        }
        return headers
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractResponseHeaders(response: NSHTTPURLResponse?): Map<String, String> {
        if (response == null) return emptyMap()
        val headers = mutableMapOf<String, String>()
        (response.allHeaderFields as? Map<String, String>)?.forEach { (key, value) ->
            headers[key] = value
        }
        return headers
    }

    @OptIn(BetaInteropApi::class)
    private fun NSData.toKotlinString(): String? =
        NSString.create(data = this, encoding = NSUTF8StringEncoding)?.toString()

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData {
        if (isEmpty()) return NSData()
        return usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
        }
    }
}
