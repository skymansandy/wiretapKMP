package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.model.ResponseSource
import dev.skymansandy.wiretap.model.RuleAction
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.repository.RuleRepository
import dev.skymansandy.wiretap.util.currentNanoTime
import dev.skymansandy.wiretap.util.currentTimeMillis
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WiretapOkHttpInterceptor : Interceptor, KoinComponent {

    private val orchestrator: WiretapOrchestrator by inject()
    private val ruleRepository: RuleRepository by inject()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val startTime = currentTimeMillis()
        val startNano = currentNanoTime()

        val matchingRule = ruleRepository.findMatchingRule(url, method)
        if (matchingRule != null && matchingRule.action == RuleAction.THROTTLE) {
            val delayMs = matchingRule.throttleDelayMs ?: 0L
            if (delayMs > 0) Thread.sleep(delayMs)
        }

        val response = chain.proceed(request)
        val durationNs = currentNanoTime() - startNano
        val durationMs = durationNs / 1_000_000

        val requestHeaders = request.headers.toMap()
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

        val logEntry = NetworkLogEntry(
            url = url,
            method = method,
            requestHeaders = requestHeaders,
            requestBody = null,
            responseCode = response.code,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            durationMs = durationMs,
            durationNs = durationNs,
            source = source,
            timestamp = currentTimeMillis(),
        )

        orchestrator.logEntry(logEntry)

        return response
    }
}
