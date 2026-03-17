package dev.skymansandy.wiretap.urlsession

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.util.currentNanoTime
import dev.skymansandy.wiretap.util.currentTimeMillis
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.allHTTPHeaderFields
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataUsingEncoding
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_time

/**
 * URLSession interceptor for Wiretap network inspection on iOS.
 *
 * Provides two APIs:
 * - [intercept]: Fire-and-forget execution with full mock/throttle rule support.
 * - [dataTask]: Returns an NSURLSessionDataTask with logging (no mock/throttle).
 *
 * Usage from Swift (with SKIE):
 * ```swift
 * let interceptor = WiretapURLSessionInterceptor(session: .shared)
 * interceptor.intercept(request: myRequest) { data, response, error in
 *     // Handle response — automatically logged by Wiretap
 * }
 * ```
 */
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
) : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val orchestrator: WiretapOrchestrator by inject()
    private val ruleRepository: RuleRepository by inject()

    companion object {
        val shared by lazy { WiretapURLSessionInterceptor() }
    }

    /**
     * Intercepts a URL request with full rule support (mock, throttle).
     * Automatically executes the request and calls [completionHandler] with the result.
     * For mock rules, the completion handler is called with mock data without network access.
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun intercept(
        request: NSURLRequest,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) {
        val url = request.URL?.absoluteString ?: ""
        val method = request.HTTPMethod ?: "GET"
        val startNano = currentNanoTime()

        val reqHeaders = extractRequestHeaders(request)
        val requestBody = request.HTTPBody?.toKotlinString()

        val matchingRule = ruleRepository.findMatchingRule(url, method, reqHeaders, requestBody)

        val logEntryId = orchestrator.logRequest(
            NetworkLogEntry(
                url = url,
                method = method,
                requestHeaders = reqHeaders,
                requestBody = requestBody,
                timestamp = currentTimeMillis(),
            ),
        )

        if (matchingRule?.action == RuleAction.MOCK) {
            handleMockResponse(
                logEntryId, url, method, reqHeaders, requestBody,
                matchingRule, startNano, completionHandler,
            )
            return
        }

        val executeRequest: () -> Unit = {
            session.dataTaskWithRequest(request) { data, response, error ->
                val durationNs = currentNanoTime() - startNano
                val httpResponse = response as? NSHTTPURLResponse

                logResponse(
                    logEntryId, url, method, reqHeaders, requestBody,
                    httpResponse, data, error, durationNs, matchingRule,
                )

                completionHandler(data, httpResponse, error)
            }.resume()
        }

        if (matchingRule?.action == RuleAction.THROTTLE) {
            val minDelay = matchingRule.throttleDelayMs ?: 0L
            val maxDelay = matchingRule.throttleDelayMaxMs ?: minDelay
            val delayMs = if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
            if (delayMs > 0) {
                val delayNs = delayMs * 1_000_000
                dispatch_after(
                    dispatch_time(DISPATCH_TIME_NOW, delayNs),
                    dispatch_get_global_queue(0.toLong(), 0u),
                ) {
                    executeRequest()
                }
                return
            }
        }

        executeRequest()
    }

    /**
     * Creates a data task with Wiretap logging. Returns the task — the caller must call resume().
     * Note: Mock/throttle rules are NOT applied. Use [intercept] for full rule support.
     */
    fun dataTask(
        request: NSURLRequest,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask {
        val url = request.URL?.absoluteString ?: ""
        val method = request.HTTPMethod ?: "GET"
        val startNano = currentNanoTime()

        val reqHeaders = extractRequestHeaders(request)
        val requestBody = request.HTTPBody?.toKotlinString()

        val logEntryId = orchestrator.logRequest(
            NetworkLogEntry(
                url = url,
                method = method,
                requestHeaders = reqHeaders,
                requestBody = requestBody,
                timestamp = currentTimeMillis(),
            ),
        )

        return session.dataTaskWithRequest(request) { data, response, error ->
            val durationNs = currentNanoTime() - startNano
            val httpResponse = response as? NSHTTPURLResponse

            logResponse(
                logEntryId, url, method, reqHeaders, requestBody,
                httpResponse, data, error, durationNs, null,
            )

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

    @OptIn(BetaInteropApi::class)
    private fun handleMockResponse(
        logEntryId: Long,
        url: String,
        method: String,
        reqHeaders: Map<String, String>,
        requestBody: String?,
        matchingRule: WiretapRule,
        startNano: Long,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) {
        val durationNs = currentNanoTime() - startNano
        val mockCode = matchingRule.mockResponseCode ?: 200
        val mockHeaders = matchingRule.mockResponseHeaders ?: emptyMap()
        val mockBody = matchingRule.mockResponseBody

        orchestrator.updateEntry(
            NetworkLogEntry(
                id = logEntryId,
                url = url,
                method = method,
                requestHeaders = reqHeaders,
                requestBody = requestBody,
                responseCode = mockCode,
                responseHeaders = mockHeaders,
                responseBody = mockBody,
                durationMs = durationNs / 1_000_000,
                durationNs = durationNs,
                source = ResponseSource.MOCK,
                timestamp = currentTimeMillis(),
                matchedRuleId = matchingRule.id,
            ),
        )

        val mockData = mockBody?.let {
            it.encodeToByteArray().toNSData()
        }
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

    private fun logResponse(
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
            ?: if (error != null) 0 else NetworkLogEntry.RESPONSE_CODE_IN_PROGRESS

        val responseHeaders = extractResponseHeaders(httpResponse)
        val responseBody = if (error != null) {
            error.localizedDescription
        } else {
            data?.toKotlinString()
        }

        val source = when (matchingRule?.action) {
            RuleAction.THROTTLE -> ResponseSource.THROTTLE
            else -> ResponseSource.NETWORK
        }

        orchestrator.updateEntry(
            NetworkLogEntry(
                id = logEntryId,
                url = url,
                method = method,
                requestHeaders = reqHeaders,
                requestBody = requestBody,
                responseCode = responseCode,
                responseHeaders = responseHeaders,
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
