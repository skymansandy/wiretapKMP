package dev.skymansandy.wiretapsample.model

import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private fun formatResponse(response: HttpResponse, body: String): String {

    val headers = response.headers.entries().joinToString("\n") { (key, values) ->
        "$key: ${values.joinToString(", ")}"
    }
    return buildString {
        appendLine("HTTP ${response.status.value} ${response.status.description}")
        appendLine(headers)
        appendLine()
        append(body)
    }
}

internal val httpActions = listOf(
    ApiAction("GET /get (HTTP)", ActionCategory.Success) { client, onStatus ->
        onStatus("GET /get ...")
        val response = client.get("http://httpbin.org/get")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("GET /posts/1", ActionCategory.Success) { client, onStatus ->
        onStatus("GET /posts/1 ...")
        val response = client.get("https://jsonplaceholder.typicode.com/posts/1")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("GET large json", ActionCategory.Success) { client, onStatus ->
        onStatus("GET /users ...")
        @Suppress("MaxLineLength")
        val url =
            "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json"
        val response = client.get(url)
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("GET /comments", ActionCategory.Success) { client, onStatus ->
        onStatus("GET /posts/1/comments ...")
        val response = client.get("https://jsonplaceholder.typicode.com/posts/1/comments")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("POST /posts", ActionCategory.Success) { client, onStatus ->
        onStatus("POST /posts ...")
        val response = client.post("https://jsonplaceholder.typicode.com/posts") {
            header("Content-Type", "application/json")
            setBody("""{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}""")
        }
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("301 Redirect", ActionCategory.Redirect) { client, onStatus ->
        onStatus("GET /redirect/1 ...")
        val response = client.get("https://httpbin.org/redirect/1")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("404 Not Found", ActionCategory.ClientError) { client, onStatus ->
        onStatus("GET /status/404 ...")
        val response = client.get("https://httpbin.org/status/404")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("500 Error", ActionCategory.ServerError) { client, onStatus ->
        onStatus("GET /status/500 ...")
        val response = client.get("https://httpbin.org/status/500")
        onStatus(formatResponse(response, response.bodyAsText()))
    },
    ApiAction("Timeout (3s)", ActionCategory.Timeout) { client, onStatus ->
        onStatus("GET /delay/10 (3s timeout) ...")
        client.get("https://httpbin.org/delay/10") {
            timeout { requestTimeoutMillis = 3.seconds.inWholeMilliseconds }
        }
        onStatus("Unexpected success")
    },
    ApiAction("Cancel in 1s", ActionCategory.Cancel) { client, onStatus ->
        coroutineScope {
            onStatus("Starting request for cancellation...")
            val job = launch {
                try {
                    client.get("https://httpbin.org/delay/10")
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // ignored
                }
            }
            delay(1.seconds)
            job.cancel()
            onStatus("Request cancelled!")
        }
    },
)
