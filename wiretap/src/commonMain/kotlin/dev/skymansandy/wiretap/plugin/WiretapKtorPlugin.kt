package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.model.NetworkLogEntry
import dev.skymansandy.wiretap.model.ResponseSource
import dev.skymansandy.wiretap.model.RuleAction
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.repository.RuleRepository
import dev.skymansandy.wiretap.util.currentTimeMillis
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val RequestTimestampKey = AttributeKey<Long>("WiretapRequestTimestamp")

val WiretapKtorPlugin = createClientPlugin("WiretapPlugin") {

    val deps = WiretapDeps()

    onRequest { request, _ ->
        request.attributes.put(RequestTimestampKey, currentTimeMillis())

        val url = request.url.buildString()
        val method = request.method.value
        val matchingRule = deps.ruleRepository.findMatchingRule(url, method)

        if (matchingRule != null && matchingRule.action == RuleAction.THROTTLE) {
            val delayMs = matchingRule.throttleDelayMs ?: 0L
            if (delayMs > 0) delay(delayMs)
        }
    }

    onResponse { response ->
        val request = response.request
        val startTime = request.attributes.getOrNull(RequestTimestampKey)
            ?: currentTimeMillis()
        val durationMs = currentTimeMillis() - startTime

        val url = request.url.toString()
        val method = request.method.value

        val requestHeaders = request.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }
        val responseHeaders = response.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }

        val responseBody = try {
            response.bodyAsText()
        } catch (_: Exception) {
            null
        }

        val matchingRule = deps.ruleRepository.findMatchingRule(url, method)
        val source = when (matchingRule?.action) {
            RuleAction.THROTTLE -> ResponseSource.THROTTLE
            else -> ResponseSource.NETWORK
        }

        val logEntry = NetworkLogEntry(
            url = url,
            method = method,
            requestHeaders = requestHeaders,
            requestBody = null,
            responseCode = response.status.value,
            responseHeaders = responseHeaders,
            responseBody = responseBody,
            durationMs = durationMs,
            source = source,
            timestamp = currentTimeMillis(),
        )

        deps.orchestrator.logEntry(logEntry)
    }
}

private class WiretapDeps : KoinComponent {
    val orchestrator: WiretapOrchestrator by inject()
    val ruleRepository: RuleRepository by inject()
}
