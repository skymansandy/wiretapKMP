package dev.skymansandy.wiretap.okhttp

import androidx.compose.ui.graphics.Color
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal fun createOkHttpClient(): OkHttpClient =
    OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor())
        .build()

internal data class OkHttpAction(
    val label: String,
    val color: Color,
    val action: (OkHttpClient, (String) -> Unit) -> Unit,
)

private val ColorSuccess = Color(0xFF2E7D32)
private val ColorRedirect = Color(0xFF1565C0)
private val ColorClientError = Color(0xFFE65100)
private val ColorServerError = Color(0xFFC62828)
private val ColorTimeout = Color(0xFF616161)
private val ColorCancel = Color(0xFF616161)

private fun Response.format(): String =
    buildString {
        appendLine("HTTP $code")
        appendLine(headers)
        appendLine()
        append(body?.string() ?: "")
    }

private fun executeGet(
    client: OkHttpClient,
    url: String,
    onStatus: (String) -> Unit,
    statusPrefix: String,
) {

    onStatus("$statusPrefix ...")
    val response = client.newCall(
        Request.Builder().url(url).build(),
    ).execute()
    onStatus(response.format())
}

internal val okHttpActions = listOf(
    OkHttpAction("GET /get (HTTP)", ColorSuccess) { client, onStatus ->
        executeGet(client, "http://httpbin.org/get", onStatus, "GET /get")
    },
    OkHttpAction("GET /posts/1", ColorSuccess) { client, onStatus ->
        executeGet(client, "https://jsonplaceholder.typicode.com/posts/1", onStatus, "GET /posts/1")
    },
    OkHttpAction("GET large json", ColorSuccess) { client, onStatus ->
        @Suppress("MaxLineLength")
        val url =
            "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json"
        executeGet(client, url, onStatus, "GET /users")
    },
    OkHttpAction("GET /comments", ColorSuccess) { client, onStatus ->
        executeGet(
            client,
            "https://jsonplaceholder.typicode.com/posts/1/comments",
            onStatus,
            "GET /posts/1/comments",
        )
    },
    OkHttpAction("POST /posts", ColorSuccess) { client, onStatus ->
        onStatus("POST /posts ...")
        val jsonBody = """{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}"""
        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
        onStatus(client.newCall(request).execute().format())
    },
    OkHttpAction("301 Redirect", ColorRedirect) { client, onStatus ->
        executeGet(client, "https://httpbin.org/redirect/1", onStatus, "GET /redirect/1")
    },
    OkHttpAction("404 Not Found", ColorClientError) { client, onStatus ->
        executeGet(client, "https://httpbin.org/status/404", onStatus, "GET /status/404")
    },
    OkHttpAction("500 Error", ColorServerError) { client, onStatus ->
        executeGet(client, "https://httpbin.org/status/500", onStatus, "GET /status/500")
    },
    OkHttpAction("Timeout (3s)", ColorTimeout) { client, onStatus ->
        onStatus("GET /delay/10 (3s timeout) ...")
        val timeoutClient = client.newBuilder()
            .callTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("https://httpbin.org/delay/10")
            .build()
        timeoutClient.newCall(request).execute()
        onStatus("Unexpected success")
    },
    OkHttpAction("Cancel in 1s", ColorCancel) { client, onStatus ->
        onStatus("Starting request for cancellation...")
        val request = Request.Builder()
            .url("https://httpbin.org/delay/10")
            .build()
        val call = client.newCall(request)
        val cancelThread = Thread {
            Thread.sleep(1000)
            call.cancel()
        }.apply { isDaemon = true }
        cancelThread.start()
        try {
            call.execute()
        } catch (_: Exception) {
            // expected cancellation
        }
        onStatus("Request cancelled!")
    },
)
