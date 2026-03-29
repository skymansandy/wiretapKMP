package dev.skymansandy.wiretapsample.model

import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_BODY_DISPLAY_LENGTH = 16_384

private fun formatResponse(response: HttpResponse, body: String): String {
    val headers = response.headers.entries().joinToString("\n") { (key, values) ->
        "$key: ${values.joinToString(", ")}"
    }
    val truncatedBody = if (body.length > MAX_BODY_DISPLAY_LENGTH) {
        body.take(MAX_BODY_DISPLAY_LENGTH) + "\n\n… (truncated ${body.length - MAX_BODY_DISPLAY_LENGTH} chars)"
    } else {
        body
    }
    return buildString {
        appendLine("HTTP ${response.status.value} ${response.status.description}")
        appendLine(headers)
        appendLine()
        append(truncatedBody)
    }
}

val ktorHttpActions: List<KtorApiAction> = httpTestCases.map { case ->
    KtorApiAction(case.label, case.category) { client, onStatus ->
        onStatus("${case.statusPrefix} ...")
        when (case) {
            is HttpTestCase.Request -> {
                val response = when (case.method) {
                    HttpMethod.GET -> client.get(case.url) {
                        case.headers.forEach { (k, v) -> header(k, v) }
                    }
                    HttpMethod.POST -> client.post(case.url) {
                        case.contentType?.let { header("Content-Type", it) }
                        case.body?.let { setBody(it) }
                        case.headers.forEach { (k, v) -> header(k, v) }
                    }
                }
                onStatus(formatResponse(response, response.bodyAsText()))
            }

            is HttpTestCase.Timeout -> {
                client.get(case.url) {
                    timeout { requestTimeoutMillis = case.timeoutMs }
                }
                onStatus("Unexpected success")
            }

            is HttpTestCase.Cancel -> coroutineScope {
                val job = launch {
                    try {
                        client.get(case.url)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // ignored
                    }
                }
                delay(case.cancelAfterMs.milliseconds)
                job.cancel()
                onStatus("Request cancelled!")
            }

            is HttpTestCase.Burst -> coroutineScope {
                for (i in 1..case.count) {
                    launch {
                        val response = client.get("${case.url}$i")
                        onStatus("Burst $i/${case.count}: HTTP ${response.status.value}")
                    }
                    if (i < case.count) delay(case.intervalMs.milliseconds)
                }
            }

            is HttpTestCase.RapidCancel -> coroutineScope {
                var previousJob: Job? = null
                for (i in 1..case.count) {
                    delay(10.milliseconds)
                    previousJob?.cancel()
                    previousJob = launch {
                        try {
                            val response = client.get("${case.url}$i")
                            onStatus("Request $i/${case.count}: HTTP ${response.status.value}")
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // ignored
                        }
                    }
                }
            }
        }
    }
}
