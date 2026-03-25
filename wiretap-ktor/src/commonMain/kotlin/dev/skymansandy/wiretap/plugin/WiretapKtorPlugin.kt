package dev.skymansandy.wiretap.plugin

import dev.skymansandy.wiretap.config.LogRetention
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.config.applyHeaderAction
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.usecase.FindMatchingRuleUseCase
import dev.skymansandy.wiretap.helper.util.currentNanoTime
import dev.skymansandy.wiretap.helper.util.currentTimeMillis
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
 * Install in your [HttpClient] configuration:
 * ```kotlin
 * HttpClient {
 *     install(WiretapKtorPlugin) {
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
 * WebSocket upgrade requests (101) are skipped — use [WiretapKtorWebSocketPlugin] for those.
 *
 * @see WiretapConfig
 * @see WiretapKtorWebSocketPlugin
 */
@OptIn(InternalAPI::class, ExperimentalAtomicApi::class)
val WiretapKtorPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {

    val config = pluginConfig
    val deps = WiretapDeps()
    val sessionInitialized = AtomicBoolean(false)

    onRequest { request, _ ->
        request.attributes.put(RequestTimestampKey, currentTimeMillis())
        request.attributes.put(RequestNanoTimestampKey, currentNanoTime())
    }

    on(Send) { request ->
        if (!config.enabled) return@on proceed(request)

        // Retention cleanup: runs once per plugin installation
        if (sessionInitialized.compareAndSet(expectedValue = false, newValue = true)) {
            deps.applyLogRetention(config.logRetention)
        }

        // Skip WebSocket upgrade requests — handled by WiretapKtorWebSocketPlugin
        val upgradeHeader = request.headers.getAll("Upgrade")
        val isWebSocketUpgrade =
            upgradeHeader?.any { it.equals("websocket", ignoreCase = true) } == true
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

        val matchingRule = deps.findMatchingRule(url, method, requestHeaders, requestBody)
        if (matchingRule != null) {
            request.attributes.put(MatchedRuleKey, matchingRule)
        }

        // Log request immediately so it appears in the UI (gated by shouldLog).
        // NonCancellable ensures the save completes and returns the ID even if the
        // coroutine is cancelled mid-flight, preventing orphaned "..." entries.
        val logEntryId = if (config.shouldLog(url, method)) {
            withContext(NonCancellable) {
                deps.orchestrator.logHttpAndGetId(
                    HttpLogEntry(
                        url = url,
                        method = method,
                        requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                        requestBody = requestBody,
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
            // but before onResponse fires, mark the entry as cancelled.
            // The conditional SQL only updates entries still at -2 (in-progress),
            // so it won't overwrite a legitimate response logged by onResponse.
            coroutineContext[Job]?.invokeOnCompletion { cause ->
                if (cause != null) {
                    runBlocking {
                        deps.orchestrator.markHttpCancelledIfInProgress(logEntryId)
                    }
                }
            }
        }

        try {
            when (val action = matchingRule?.action) {
                is RuleAction.Mock -> {
                    action.throttleDelayMs?.let { minDelay ->
                        val maxDelay = action.throttleDelayMaxMs ?: minDelay
                        delay(if (maxDelay > minDelay) (minDelay..maxDelay).random() else minDelay)
                    }

                    val statusCode = HttpStatusCode.fromValue(action.responseCode)
                    val mockHeaders = Headers.build {
                        action.responseHeaders?.forEach { (k, v) -> append(k, v) }
                    }
                    val responseBody =
                        action.responseBody?.encodeToByteArray() ?: ByteArray(0)
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
                            request.attributes.getOrNull(RequestNanoTimestampKey)
                                ?: currentNanoTime()
                        val durationNs = currentNanoTime() - startNano
                        val mockRespHeaders = action.responseHeaders ?: emptyMap()
                        deps.orchestrator.updateHttp(
                            HttpLogEntry(
                                id = logEntryId,
                                url = url,
                                method = method,
                                requestHeaders = requestHeaders.applyHeaderAction(config.headerAction),
                                requestBody = requestBody,
                                responseCode = statusCode.value,
                                responseHeaders = mockRespHeaders.applyHeaderAction(config.headerAction),
                                responseBody = action.responseBody,
                                durationMs = durationNs / 1_000_000,
                                durationNs = durationNs,
                                source = ResponseSource.Mock,
                                timestamp = currentTimeMillis(),
                                matchedRuleId = matchingRule.id,
                            ),
                        )
                    }

                    call
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
                // the coroutine is cancelled, so the entry moves from "..." to "!!!".
                withContext(NonCancellable) {
                    deps.orchestrator.updateHttp(
                        HttpLogEntry(
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
            deps.orchestrator.deleteHttpLog(logEntryId)
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
            else -> ResponseSource.Network
        }

        val protocol = response.version.let { "${it.name}/${it.major}.${it.minor}" }

        // NonCancellable ensures the DB update completes even when
        // the coroutine is cancelled mid-response, so the entry doesn't stay stuck at "...".
        withContext(NonCancellable) {
            deps.orchestrator.updateHttp(
                HttpLogEntry(
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
}

private class WiretapDeps : KoinComponent {

    override fun getKoin(): Koin = WiretapDi.getKoin()
    val orchestrator: WiretapOrchestrator by inject()
    val findMatchingRule: FindMatchingRuleUseCase by inject()

    suspend fun applyLogRetention(logRetention: LogRetention) {
        when (logRetention) {
            is LogRetention.AppSession -> orchestrator.clearHttpLogs()

            is LogRetention.Days -> {
                val cutoff = currentTimeMillis() - logRetention.days * 24L * 60 * 60 * 1000
                orchestrator.purgeHttpLogsOlderThan(cutoff)
            }

            else -> Unit
        }
    }
}
