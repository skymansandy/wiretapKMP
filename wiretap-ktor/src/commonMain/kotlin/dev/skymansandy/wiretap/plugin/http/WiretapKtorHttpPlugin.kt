/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.plugin.http

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
import dev.skymansandy.wiretap.helper.util.truncateBody
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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private val RequestTimestampKey = AttributeKey<Long>("WiretapRequestTimestamp")
private val RequestNanoTimestampKey = AttributeKey<Long>("WiretapRequestNanoTimestamp")
private val MatchedRuleKey = AttributeKey<WiretapRule>("WiretapMatchedRule")
private val LogEntryIdKey = AttributeKey<Long>("WiretapLogEntryId")

/**
 * Ktor client plugin for Wiretap network inspection.
 *
 * Intercepts HTTP requests and responses to log them via the Wiretap orchestrator.
 * Supports mock and throttle rules — matching requests can return fake responses
 * or be delayed before reaching the network.
 *
 * Install in your [io.ktor.client.HttpClient] configuration:
 * ```kotlin
 * HttpClient {
 *     install(WiretapKtorHttpPlugin) {
 *         shouldLog = { url, _ -> url.contains("/api/") }
 *         headerAction = { key ->
 *             if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
 *             else HeaderAction.Keep
 *         }
 *         logRetention = LogRetention.Days(7)
 *     }
 * }
 * ```
 *
 * WebSocket upgrade requests (101) are skipped — use [dev.skymansandy.wiretap.plugin.ws.WiretapKtorWebSocketPlugin] for those.
 *
 * @see WiretapConfig
 * @see dev.skymansandy.wiretap.plugin.ws.WiretapKtorWebSocketPlugin
 */
@OptIn(InternalAPI::class, ExperimentalAtomicApi::class)
val WiretapKtorHttpPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    val config = pluginConfig
    val deps = WiretapDeps()
    val sessionInitialized = AtomicBoolean(false)

    // Retention cleanup: runs once per plugin installation
    suspend fun initSessionIfNeeded() {
        if (sessionInitialized.compareAndSet(expectedValue = false, newValue = true)) {
            when (val logRetention = config.logRetention) {
                LogRetention.Forever -> Unit
                is LogRetention.AppSession -> deps.httpLogManager.clearHttpLogs()
                is LogRetention.Days -> {
                    val cutoff = currentTimeMillis() - logRetention.days * 24L * 60 * 60 * 1000
                    deps.httpLogManager.purgeHttpLogsOlderThan(cutoff)
                }
            }
        }
    }

    onRequest { request, _ ->
        request.attributes.put(RequestTimestampKey, currentTimeMillis())
        request.attributes.put(RequestNanoTimestampKey, currentNanoTime())
    }

    on(Send) { request ->
        if (!config.enabled) return@on proceed(request)

        initSessionIfNeeded()

        // Skip WebSocket upgrade requests — handled by WiretapKtorWebSocketPlugin
        val upgradeHeader = request.headers.getAll("Upgrade")
        val isWebSocketUpgrade =
            upgradeHeader?.any { it.equals("websocket", ignoreCase = true) } == true
        val urlString = request.url.buildString()
        val isWebSocketScheme =
            urlString.startsWith("ws://") || urlString.startsWith("wss://")
        if (isWebSocketUpgrade || isWebSocketScheme) return@on proceed(request)

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

        val matchingRule = deps.findMatchingRule(url, method, requestHeaders, requestBody)
        if (matchingRule != null) {
            request.attributes.put(MatchedRuleKey, matchingRule)
        }

        // Log request immediately so it appears in the UI (gated by shouldLog).
        // NonCancellable ensures the save completes and returns the ID even if the
        // coroutine is canceled mid-flight, preventing orphaned entries.
        val logEntryId = if (config.shouldLog(url, method)) {
            withContext(NonCancellable) {
                deps.httpLogManager.logHttpAndGetId(
                    HttpLog(
                        url = url,
                        method = method,
                        requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody.truncateBody(config.maxContentLength),
                        timestamp = currentTimeMillis(),
                    ),
                )
            }
        } else {
            -1L
        }

        if (logEntryId >= 0) {
            request.attributes.put(LogEntryIdKey, logEntryId)

            // Safety net: if the coroutine is cancelled after proceed() returns
            // but before onResponse fires, mark the entry as canceled.
            // The conditional SQL only updates entries still at -2 (in-progress),
            // so it won't overwrite a legitimate response logged by onResponse.
            coroutineContext[Job]?.invokeOnCompletion { cause ->
                if (cause != null) {
                    runBlocking {
                        deps.httpLogManager.markHttpCancelledIfInProgress(logEntryId)
                    }
                }
            }
        }

        try {
            when (val action = matchingRule?.action) {
                is RuleAction.Mock -> {
                    buildMockCall(
                        action.responseCode, action.responseHeaders, action.responseBody,
                        ResponseSource.Mock, logEntryId, url, method,
                        requestHeaders, requestBody, request, matchingRule, config, deps, client,
                    )
                }

                is RuleAction.MockAndThrottle -> {
                    val minDelay = action.delayMs
                    val maxDelay = action.delayMaxMs ?: minDelay
                    val delayMs =
                        if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
                    if (delayMs > 0) delay(delayMs)

                    buildMockCall(
                        action.responseCode, action.responseHeaders, action.responseBody,
                        ResponseSource.MockAndThrottle, logEntryId, url, method,
                        requestHeaders, requestBody, request, matchingRule, config, deps, client,
                    )
                }

                is RuleAction.Throttle -> {
                    val minDelay = action.delayMs
                    val maxDelay = action.delayMaxMs ?: minDelay
                    val delayMs =
                        if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay
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
                // NonCancellable ensures the DB update completes even when
                // the coroutine is canceled, so the entry moves from "..." to "!!!".
                withContext(NonCancellable) {
                    deps.httpLogManager.updateHttp(
                        HttpLog(
                            id = logEntryId,
                            url = url,
                            method = method,
                            requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                            requestBody = requestBody.truncateBody(config.maxContentLength),
                            responseCode = if (e is CancellationException) -1 else 0,
                            responseHeaders = emptyMap(),
                            responseBody = (e.message ?: e::class.simpleName ?: "Unknown error")
                                .truncateBody(config.maxContentLength),
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
    }

    onResponse { response ->
        val request = response.request
        val logEntryId = request.attributes.getOrNull(LogEntryIdKey) ?: return@onResponse

        // WebSocket upgrade (101) — remove any HTTP log entry; socket plugin handles it
        if (response.status.value == 101) {
            deps.httpLogManager.deleteHttpLog(logEntryId)
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
            is RuleAction.Mock -> ResponseSource.Mock
            is RuleAction.Throttle -> ResponseSource.Throttle
            is RuleAction.MockAndThrottle -> ResponseSource.MockAndThrottle
            else -> ResponseSource.Network
        }

        val protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" }

        // NonCancellable ensures the DB update completes even when
        // the coroutine is canceled mid-response, so the entry doesn't stay stuck at "...".
        withContext(NonCancellable) {
            deps.httpLogManager.updateHttp(
                HttpLog(
                    id = logEntryId,
                    url = url,
                    method = method,
                    requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                    requestBody = null,
                    responseCode = response.status.value,
                    responseHeaders = responseHeaders.applyHeaderAction(config.headerAction),
                    responseBody = responseBody.truncateBody(config.maxContentLength),
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
}

private class WiretapDeps : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()

    val httpLogManager: HttpLogManager by inject()
    val findMatchingRule: FindMatchingRuleUseCase by inject()
}

@Suppress("LongParameterList")
@OptIn(InternalAPI::class)
private suspend fun buildMockCall(
    responseCode: Int,
    responseHeaders: Map<String, String>?,
    responseBody: String?,
    source: ResponseSource,
    logEntryId: Long,
    url: String,
    method: String,
    requestHeaders: Map<String, String>,
    requestBody: String?,
    request: io.ktor.client.request.HttpRequestBuilder,
    matchingRule: WiretapRule,
    config: WiretapConfig,
    deps: WiretapDeps,
    httpClient: io.ktor.client.HttpClient,
): HttpClientCall {
    val statusCode = HttpStatusCode.fromValue(responseCode)
    val mockHeaders = Headers.build {
        responseHeaders?.forEach { (k, v) -> append(k, v) }
    }
    val body = responseBody?.encodeToByteArray() ?: ByteArray(0)
    val call = HttpClientCall(
        client = httpClient,
        requestData = request.build(),
        responseData = HttpResponseData(
            statusCode = statusCode,
            requestTime = GMTDate(),
            headers = mockHeaders,
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(body),
            callContext = kotlin.coroutines.coroutineContext + Job(),
        ),
    )

    if (logEntryId >= 0) {
        val startNano = request.attributes.getOrNull(RequestNanoTimestampKey)
            ?: currentNanoTime()
        val durationNs = currentNanoTime() - startNano
        val mockRespHeaders = responseHeaders ?: emptyMap()
        deps.httpLogManager.updateHttp(
            HttpLog(
                id = logEntryId,
                url = url,
                method = method,
                requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                requestBody = requestBody.truncateBody(config.maxContentLength),
                responseCode = statusCode.value,
                responseHeaders = mockRespHeaders.applyHeaderAction(config.headerAction),
                responseBody = responseBody.truncateBody(config.maxContentLength),
                durationMs = durationNs / 1_000_000,
                durationNs = durationNs,
                source = source,
                timestamp = currentTimeMillis(),
                matchedRuleId = matchingRule.id,
            ),
        )
    }

    return call
}
