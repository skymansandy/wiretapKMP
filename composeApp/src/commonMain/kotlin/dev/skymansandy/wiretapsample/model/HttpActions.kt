package dev.skymansandy.wiretapsample.model

import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_BODY_DISPLAY_LENGTH = 16_384

private suspend fun formatResponse(response: HttpResponse): String {
    val body = response.bodyAsText()
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
    KtorApiAction(case.label, case.category) { apis, onStatus ->
        onStatus("${case.statusPrefix} ...")
        when (case) {
            is HttpTestCase.DeserializeJson -> {
                val post = apis.jsonPlaceholder.getPost(1)
                onStatus("Deserialized post #${post.id}: \"${post.title}\"")
            }

            is HttpTestCase.Request -> {
                val response = when (case.endpoint) {
                    Endpoint.HttpBinGet -> apis.httpBinHttp.get()
                    Endpoint.JsonPlaceholderGetPost -> apis.jsonPlaceholder.getPostRaw(1)
                    Endpoint.JsonPlaceholderGetComments -> apis.jsonPlaceholder.getComments(1)
                    Endpoint.JsonPlaceholderCreatePost -> apis.jsonPlaceholder.createPost(case.body!!)
                    Endpoint.HttpBinGetHeaders -> apis.httpBin.getHeaders(case.headers)
                    Endpoint.HttpBinPostAnything -> apis.httpBin.postAnything(case.body!!, case.headers)
                    Endpoint.HttpBingoQueryAnything -> apis.httpBingo.queryAnything(case.body!!)
                    Endpoint.HttpBinStatus404 -> apis.httpBin.getStatus(404)
                    Endpoint.HttpBinStatus500 -> apis.httpBin.getStatus(500)
                    Endpoint.HttpBinRedirect -> apis.httpBin.redirect(1)
                    Endpoint.ExternalUrl -> apis.external.getByUrl(case.url)
                }
                onStatus(formatResponse(response))
            }

            is HttpTestCase.Timeout -> {
                apis.client.get(case.url) {
                    timeout { requestTimeoutMillis = case.timeoutMs }
                }
                onStatus("Unexpected success")
            }

            is HttpTestCase.Cancel -> coroutineScope {
                val job = launch {
                    try {
                        apis.httpBin.delay(10)
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
                        val response = apis.jsonPlaceholder.getPostRaw(i)
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
                            val response = apis.jsonPlaceholder.getPostRaw(i)
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
