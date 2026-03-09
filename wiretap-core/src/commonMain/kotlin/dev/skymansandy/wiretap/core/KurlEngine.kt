package dev.skymansandy.wiretap.core

import dev.skymansandy.wiretap.core.model.KurlRequest
import dev.skymansandy.wiretap.core.model.KurlResponse
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlin.time.TimeSource

class KurlEngine {

    private val client = createHttpClient()

    suspend fun execute(request: KurlRequest): KurlResponse {
        val mark = TimeSource.Monotonic.markNow()

        val response = client.request(request.url) {
            method = HttpMethod(request.method)

            request.headers.forEach { (key, value) ->
                header(key, value)
            }

            url {
                request.queryParams.forEach { (key, value) ->
                    parameters.append(key, value)
                }
            }

            if (!request.body.isNullOrEmpty()) {
                setBody(request.body)
            }
        }

        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        val bodyText = response.bodyAsText()

        val httpVersion = "HTTP/${response.version.major}.${response.version.minor}"

        return KurlResponse(
            statusCode = response.status.value,
            statusText = response.status.description,
            headers = response.headers.entries()
                .associate { it.key to it.value.joinToString(", ") },
            body = bodyText,
            timeMs = elapsedMs,
            sizeBytes = bodyText.encodeToByteArray().size.toLong(),
            networkInfo = buildNetworkInfo(request.url, httpVersion)
        )
    }

    fun close() {
        client.close()
    }
}