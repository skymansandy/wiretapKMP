package dev.skymansandy.wiretapsample.model

import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal val httpActions = listOf(
    ApiAction("GET /get (HTTP)", ActionCategory.SUCCESS) { client, onStatus ->
        onStatus("GET /get ...")
        val response = client.get("http://httpbin.org/get")
        onStatus("GET /get -> ${response.status.value}")
    },
    ApiAction("GET /posts/1", ActionCategory.SUCCESS) { client, onStatus ->
        onStatus("GET /posts/1 ...")
        val response = client.get("https://jsonplaceholder.typicode.com/posts/1")
        onStatus("GET /posts/1 -> ${response.status.value}")
    },
    ApiAction("GET large json", ActionCategory.SUCCESS) { client, onStatus ->
        onStatus("GET /users ...")
        @Suppress("MaxLineLength")
        val url = "https://gist.githubusercontent.com/gcollazo/884a489a50aec7b53765405f40c6fbd1/raw/49d1568c34090587ac82e80612a9c350108b62c5/sample.json"
        val response = client.get(url)
        onStatus("GET /Contents.json -> ${response.status.value}")
    },
    ApiAction("GET /comments", ActionCategory.SUCCESS) { client, onStatus ->
        onStatus("GET /posts/1/comments ...")
        val response = client.get("https://jsonplaceholder.typicode.com/posts/1/comments")
        onStatus("GET /comments -> ${response.status.value}")
    },
    ApiAction("POST /posts", ActionCategory.SUCCESS) { client, onStatus ->
        onStatus("POST /posts ...")
        val response = client.post("https://jsonplaceholder.typicode.com/posts") {
            header("Content-Type", "application/json")
            setBody("""{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}""")
        }
        onStatus("POST /posts -> ${response.status.value}")
    },
    ApiAction("301 Redirect", ActionCategory.REDIRECT) { client, onStatus ->
        onStatus("GET /redirect/1 ...")
        val response = client.get("https://httpbin.org/redirect/1")
        onStatus("GET /redirect/1 -> ${response.status.value}")
    },
    ApiAction("404 Not Found", ActionCategory.CLIENT_ERROR) { client, onStatus ->
        onStatus("GET /status/404 ...")
        val response = client.get("https://httpbin.org/status/404")
        onStatus("GET /status/404 -> ${response.status.value}")
    },
    ApiAction("500 Error", ActionCategory.SERVER_ERROR) { client, onStatus ->
        onStatus("GET /status/500 ...")
        val response = client.get("https://httpbin.org/status/500")
        onStatus("GET /status/500 -> ${response.status.value}")
    },
    ApiAction("Timeout (1s)", ActionCategory.EDGE_CASE) { client, onStatus ->
        onStatus("GET /delay/10 (1s timeout) ...")
        client.get("https://httpbin.org/delay/10") {
            timeout { requestTimeoutMillis = 6000 }
        }
        onStatus("Unexpected success")
    },
    ApiAction("Cancel", ActionCategory.EDGE_CASE) { client, onStatus ->
        onStatus("Cancelling in 500ms ...")
    },
)
