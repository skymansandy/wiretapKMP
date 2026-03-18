package dev.skymansandy.wiretap.plugin

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
val WiretapKtorPlugin = createClientPlugin("WiretapPlugin") {

    val deps = WiretapDeps()

    onRequest { request, _ ->
        request.attributes.put(RequestTimestampKey, currentTimeMillis())
        request.attributes.put(RequestNanoTimestampKey, currentNanoTime())
    }

    on(Send) { request ->
        // Skip WebSocket upgrade requests — handled by WiretapKtorWebSocketPlugin
        val isWebSocketUpgrade = request.headers.contains("Upgrade", "websocket")
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

        // Log request immediately so it appears in the UI
        val logEntryId = deps.orchestrator.logRequest(
            NetworkLogEntry(
                url = url,
                method = method,
                requestHeaders = requestHeaders,
                requestBody = requestBody,
                timestamp = currentTimeMillis(),
            ),
        )
        request.attributes.put(LogEntryIdKey, logEntryId)

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
                            // Standalone Job so the channel's lifecycle is not tied to the
                            // on(Send) block's coroutine context, preventing "parent job is
                            // completed" when Ktor processes the mock response afterward.
                            callContext = coroutineContext + Job(),
                        ),
                    )

                    val startNano =
                        request.attributes.getOrNull(RequestNanoTimestampKey) ?: currentNanoTime()
                    val durationNs = currentNanoTime() - startNano
                    deps.orchestrator.updateEntry(
                        NetworkLogEntry(
                            id = logEntryId,
                            url = url,
                            method = method,
                            requestHeaders = requestHeaders,
                            requestBody = requestBody,
                            responseCode = statusCode.value,
                            responseHeaders = matchingRule.mockResponseHeaders ?: emptyMap(),
                            responseBody = matchingRule.mockResponseBody,
                            durationMs = durationNs / 1_000_000,
                            durationNs = durationNs,
                            source = ResponseSource.MOCK,
                            timestamp = currentTimeMillis(),
                            matchedRuleId = matchingRule.id,
                        ),
                    )

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
            val startNano =
                request.attributes.getOrNull(RequestNanoTimestampKey) ?: currentNanoTime()
            val durationNs = currentNanoTime() - startNano
            deps.orchestrator.updateEntry(
                NetworkLogEntry(
                    id = logEntryId,
                    url = url,
                    method = method,
                    requestHeaders = requestHeaders,
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
            throw e
        }
    }

    onResponse { response ->
        // Skip WebSocket upgrades — handled by WiretapKtorWebSocketPlugin
        if (response.status.value == 101) return@onResponse

        val request = response.request
        val logEntryId = request.attributes.getOrNull(LogEntryIdKey) ?: return@onResponse
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
                requestHeaders = requestHeaders,
                requestBody = null,
                responseCode = response.status.value,
                responseHeaders = responseHeaders,
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
