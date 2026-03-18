package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.config.applyHeaderAction
import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.util.currentNanoTime
import dev.skymansandy.wiretap.util.currentTimeMillis
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private val RequestTimestampKey = AttributeKey<Long>("WiretapRequestTimestamp")
private val RequestNanoTimestampKey = AttributeKey<Long>("WiretapRequestNanoTimestamp")
private val MatchedRuleKey = AttributeKey<WiretapRule>("WiretapMatchedRule")
private val LogEntryIdKey = AttributeKey<Long>("WiretapLogEntryId")

@OptIn(InternalAPI::class)
val WiretapKtorPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {

    val config = pluginConfig
    val deps = WiretapDeps()
    var sessionInitialized = false

    onRequest { request, _ ->
        request.attributes.put(RequestTimestampKey, currentTimeMillis())
        request.attributes.put(RequestNanoTimestampKey, currentNanoTime())
    }

    on(Send) { request ->
        if (!config.enabled) return@on proceed(request)

        // AppSession: clear all previous logs once per plugin installation
        if (!sessionInitialized) {
            sessionInitialized = true
            if (config.logRetention == LogRetention.AppSession) {
                deps.orchestrator.clearLogs()
            }
        }

        // Skip WebSocket upgrade requests — handled by WiretapKtorWebSocketPlugin
        val upgradeHeader = request.headers.getAll("Upgrade")
        val isWebSocketUpgrade = upgradeHeader?.any { it.equals("websocket", ignoreCase = true) } == true
        if (isWebSocketUpgrade) return@on proceed(request)

        val url = request.url.buildString()
        val method = request.method.value
        val requestHeaders = request.headers.entries()
            .associate { (key, values) -> key to values.joinToString(", ") }
        val requestBody = try {
            when (val body = request.body) {
                is OutgoingContent.ByteArrayContent -> body.bytes().decodeToString()
                is OutgoingContent.ReadChannelContent -> body.readFrom().readRemaining().readText()
                else -> null
            }
        } catch (_: Exception) {
            null
        }

        val matchingRule = deps.ruleRepository.findMatchingRule(url, method, requestHeaders, requestBody)
        if (matchingRule != null) {
            request.attributes.put(MatchedRuleKey, matchingRule)
        }

        // Days retention: prune old entries before each new capture
        val retention = config.logRetention
        if (retention is LogRetention.Days) {
            val cutoff = currentTimeMillis() - retention.days * 24L * 60 * 60 * 1000
            deps.orchestrator.purgeLogsOlderThan(cutoff)
        }

        // Log request immediately so it appears in the UI (gated by shouldLog)
        val logEntryId = if (config.shouldLog(url, method)) {
            deps.orchestrator.logRequest(
                NetworkLogEntry(
                    url = url,
                    method = method,
                    requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                    requestBody = requestBody,
                    timestamp = currentTimeMillis(),
                ),
            )
        } else {
            -1L
        }
        if (logEntryId >= 0) {
            request.attributes.put(LogEntryIdKey, logEntryId)
        }

        try {
            when (matchingRule?.action) {
                RuleAction.MOCK -> {
                    matchingRule.throttleDelayMs?.let { minDelay ->
                        val maxDelay = matchingRule.throttleDelayMaxMs ?: minDelay
                        delay(if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay)
                    }

                    val statusCode = HttpStatusCode.fromValue(matchingRule.mockResponseCode ?: 200)
                    val mockHeaders = Headers.build {
                        matchingRule.mockResponseHeaders?.forEach { (k, v) -> append(k, v) }
                    }
                    val responseBody =
                        matchingRule.mockResponseBody?.encodeToByteArray() ?: ByteArray(0)
                    val call = HttpClientCall(
                        client = client,
                        requestData = request.build(),
                        responseData = HttpResponseData(
                            statusCode = statusCode,
                            requestTime = GMTDate(),
                            headers = mockHeaders,
                            version = HttpProtocolVersion.HTTP_1_1,
                            body = ByteReadChannel(responseBody),
                            callContext = coroutineContext + Job(),
                        ),
                    )

                    if (logEntryId >= 0) {
                        val startNano =
                            request.attributes.getOrNull(RequestNanoTimestampKey) ?: currentNanoTime()
                        val durationNs = currentNanoTime() - startNano
                        val mockRespHeaders = matchingRule.mockResponseHeaders ?: emptyMap()
                        deps.orchestrator.updateEntry(
                            NetworkLogEntry(
                                id = logEntryId,
                                url = url,
                                method = method,
                                requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                                requestBody = requestBody,
                                responseCode = statusCode.value,
                                responseHeaders = mockRespHeaders.applyHeaderAction(config.headerAction),
                                responseBody = matchingRule.mockResponseBody,
                                durationMs = durationNs / 1_000_000,
                                durationNs = durationNs,
                                source = ResponseSource.MOCK,
                                timestamp = currentTimeMillis(),
                                matchedRuleId = matchingRule.id,
                            ),
                        )
                    }

                    call
                }

                RuleAction.THROTTLE -> {
                    val minDelay = matchingRule.throttleDelayMs ?: 0L
                    val maxDelay = matchingRule.throttleDelayMaxMs ?: minDelay
                    val delayMs = if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
                    if (delayMs > 0) delay(delayMs)
                    proceed(request)
                }

                else -> proceed(request)
            }
        } catch (e: Exception) {
            if (logEntryId >= 0) {
                val startNano =
                    request.attributes.getOrNull(RequestNanoTimestampKey) ?: currentNanoTime()
                val durationNs = currentNanoTime() - startNano
                deps.orchestrator.updateEntry(
                    NetworkLogEntry(
                        id = logEntryId,
                        url = url,
                        method = method,
                        requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
                        responseCode = if (e is CancellationException) -1 else 0,
                        responseHeaders = emptyMap(),
                        responseBody = e.message ?: e::class.simpleName ?: "Unknown error",
                        durationMs = durationNs / 1_000_000,
                        durationNs = durationNs,
                        source = ResponseSource.NETWORK,
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
            throw e
        }
    }

    onResponse { response ->
        val request = response.request
        val logEntryId = request.attributes.getOrNull(LogEntryIdKey) ?: return@onResponse

        // WebSocket upgrade (101) — remove any HTTP log entry; socket plugin handles it
        if (response.status.value == 101) {
            deps.orchestrator.deleteLog(logEntryId)
            return@onResponse
        }
        val startNano = request.attributes.getOrNull(RequestNanoTimestampKey) ?: currentNanoTime()
        val durationNs = currentNanoTime() - startNano
        val durationMs = durationNs / 1_000_000

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

        val source = when (request.attributes.getOrNull(MatchedRuleKey)?.action) {
            RuleAction.MOCK -> ResponseSource.MOCK
            RuleAction.THROTTLE -> ResponseSource.THROTTLE
            else -> ResponseSource.NETWORK
        }

        val protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" }

        deps.orchestrator.updateEntry(
            NetworkLogEntry(
                id = logEntryId,
                url = url,
                method = method,
                requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                requestBody = null,
                responseCode = response.status.value,
                responseHeaders = responseHeaders.applyHeaderAction(config.headerAction),
                responseBody = responseBody,
                durationMs = durationMs,
                durationNs = durationNs,
                source = source,
                timestamp = currentTimeMillis(),
                matchedRuleId = request.attributes.getOrNull(MatchedRuleKey)?.id,
                protocol = protocol,
            ),
        )
    }
}

private class WiretapDeps : KoinComponent {
    override fun getKoin(): Koin = WiretapDi.getKoin()
    val orchestrator: WiretapOrchestrator by inject()
    val ruleRepository: RuleRepository by inject()
}
