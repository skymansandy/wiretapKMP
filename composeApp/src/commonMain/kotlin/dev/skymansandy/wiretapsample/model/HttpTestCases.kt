package dev.skymansandy.wiretapsample.model

enum class HttpMethod { GET, POST }

sealed class HttpTestCase {

    abstract val label: String
    abstract val statusPrefix: String
    abstract val url: String
    abstract val category: ActionCategory

    data class Request(
        override val label: String,
        override val statusPrefix: String,
        override val url: String,
        override val category: ActionCategory,
        val method: HttpMethod = HttpMethod.GET,
        val body: String? = null,
        val contentType: String? = null,
        val headers: Map<String, String> = emptyMap(),
    ) : HttpTestCase()

    data class Timeout(
        override val label: String,
        override val statusPrefix: String,
        override val url: String,
        override val category: ActionCategory = ActionCategory.Timeout,
        val timeoutMs: Long,
    ) : HttpTestCase()

    data class Cancel(
        override val label: String,
        override val statusPrefix: String,
        override val url: String,
        override val category: ActionCategory = ActionCategory.Cancel,
        val cancelAfterMs: Long,
    ) : HttpTestCase()

    data class Burst(
        override val label: String,
        override val statusPrefix: String,
        override val url: String,
        override val category: ActionCategory = ActionCategory.Batch,
        val count: Int,
        val intervalMs: Long,
    ) : HttpTestCase()

    data class RapidCancel(
        override val label: String,
        override val statusPrefix: String,
        override val url: String,
        override val category: ActionCategory = ActionCategory.Batch,
        val count: Int,
    ) : HttpTestCase()
}

@Suppress("MaxLineLength")
val httpTestCases = listOf(
    HttpTestCase.Request(
        label = "GET /get (HTTP)",
        statusPrefix = "GET /get",
        url = "http://httpbin.org/get",
        category = ActionCategory.Success,
    ),
    HttpTestCase.Request(
        label = "GET /posts/1",
        statusPrefix = "GET /posts/1",
        url = "https://jsonplaceholder.typicode.com/posts/1",
        category = ActionCategory.Success,
    ),
    HttpTestCase.Request(
        label = "GET large json",
        statusPrefix = "GET /users",
        url = "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json",
        category = ActionCategory.Success,
    ),
    HttpTestCase.Request(
        label = "GET /comments",
        statusPrefix = "GET /posts/1/comments",
        url = "https://jsonplaceholder.typicode.com/posts/1/comments",
        category = ActionCategory.Success,
    ),
    HttpTestCase.Request(
        label = "POST /posts",
        statusPrefix = "POST /posts",
        url = "https://jsonplaceholder.typicode.com/posts",
        category = ActionCategory.Success,
        method = HttpMethod.POST,
        body = """{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}""",
        contentType = "application/json",
    ),
    HttpTestCase.Request(
        label = "GET /headers",
        statusPrefix = "GET /headers (custom)",
        url = "https://httpbin.org/headers",
        category = ActionCategory.Success,
        headers = mapOf(
            "X-Wiretap-Debug" to "true",
            "X-Request-Source" to "WiretapSampleApp",
            "X-Correlation-Id" to "abc-123-def-456",
            "Accept-Language" to "en-US,en;q=0.9",
        ),
    ),
    HttpTestCase.Request(
        label = "POST /anything",
        statusPrefix = "POST /anything (headers+body)",
        url = "https://httpbin.org/anything",
        category = ActionCategory.Success,
        method = HttpMethod.POST,
        body = """{"event":"purchase","item":"Wiretap Pro","quantity":3,"metadata":{"source":"sample-app","version":"1.0"}}""",
        contentType = "application/json",
        headers = mapOf(
            "X-Api-Key" to "sample-key-12345",
            "X-Idempotency-Key" to "idem-99887766",
            "X-Custom-Trace" to "trace-aabbccdd",
        ),
    ),
    HttpTestCase.Request(
        label = "301 Redirect",
        statusPrefix = "GET /redirect/1",
        url = "https://httpbin.org/redirect/1",
        category = ActionCategory.Redirect,
    ),
    HttpTestCase.Request(
        label = "404 Not Found",
        statusPrefix = "GET /status/404",
        url = "https://httpbin.org/status/404",
        category = ActionCategory.ClientError,
    ),
    HttpTestCase.Request(
        label = "500 Error",
        statusPrefix = "GET /status/500",
        url = "https://httpbin.org/status/500",
        category = ActionCategory.ServerError,
    ),
    HttpTestCase.Timeout(
        label = "Timeout (3s)",
        statusPrefix = "GET /delay/10 (3s timeout)",
        url = "https://httpbin.org/delay/10",
        timeoutMs = 3000,
    ),
    HttpTestCase.Cancel(
        label = "Cancel in 1s",
        statusPrefix = "Starting request for cancellation",
        url = "https://httpbin.org/delay/10",
        cancelAfterMs = 1000,
    ),
    HttpTestCase.Burst(
        label = "4 reqs @ 4s interval",
        statusPrefix = "Burst: 4 requests at 4s intervals",
        url = "https://jsonplaceholder.typicode.com/posts/",
        count = 4,
        intervalMs = 4000,
    ),
    HttpTestCase.RapidCancel(
        label = "10 reqs, cancel prev",
        statusPrefix = "Rapid cancel: 10 requests, only last completes",
        url = "https://jsonplaceholder.typicode.com/posts/",
        count = 10,
    ),
)
