package dev.skymansandy.wiretap.okhttp

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

class WiretapOkHttpInterceptor : Interceptor, KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    private val orchestrator: WiretapOrchestrator by inject()
    private val ruleRepository: RuleRepository by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
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

            orchestrator.logEntry(
                NetworkLogEntry(
                    url = url,
                    method = method,
                    requestHeaders = reqHeaders,
                    requestBody = requestBody,
                    responseCode = mockResponse.code,
                    responseHeaders = mockResponse.headers.toMap(),
                    responseBody = matchingRule.mockResponseBody,
                    durationMs = durationNs / 1_000_000,
                    durationNs = durationNs,
                    source = ResponseSource.MOCK,
                    timestamp = currentTimeMillis(),
                ),
            )
            return mockResponse
        }

        if (matchingRule?.action == RuleAction.THROTTLE) {
            val delayMs = matchingRule.throttleDelayMs ?: 0L
            if (delayMs > 0) Thread.sleep(delayMs)
        }

        val response = chain.proceed(request)
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

        orchestrator.logEntry(
            NetworkLogEntry(
                url = url,
                method = method,
                requestHeaders = reqHeaders,
                requestBody = null,
                responseCode = response.code,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                durationMs = durationMs,
                durationNs = durationNs,
                source = source,
                timestamp = currentTimeMillis(),
            ),
        )

        return response
    }
}
