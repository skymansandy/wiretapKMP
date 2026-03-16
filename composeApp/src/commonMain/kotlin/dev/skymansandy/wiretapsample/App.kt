package dev.skymansandy.wiretapsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.notification.startWiretap
import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun App() {
    WiretapTheme {
        val client = remember {
            HttpClient {
                install(WiretapKtorPlugin)
                install(HttpTimeout)
            }
        }
        val scope = rememberCoroutineScope {
            CoroutineExceptionHandler { _, e ->
                e
            }
        }
        var status by remember { mutableStateOf("Tap buttons to make requests") }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = "Wiretap Sample",
                    style = MaterialTheme.typography.headlineMedium,
                )

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(onClick = {
                    scope.launch {
                        status = "Fetching (HTTP)..."
                        try {
                            val response = client.get("http://httpbin.org/get")
                            status = "GET /get (HTTP) -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET /get (HTTP)")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Fetching posts..."
                        try {
                            val response = client.get("https://jsonplaceholder.typicode.com/posts/1")
                            status = "GET /posts/1 -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET /posts/1")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Fetching users..."
                        try {
                            @Suppress("MaxLineLength")
                            val url = "https://raw.githubusercontent.com/usuiat/Zoomable/refs/heads/main/samples/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/Contents.json"
                            val response = client.get(url)
                            status = "GET /Contents.json -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET /users")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Fetching comments..."
                        try {
                            val response = client.get("https://jsonplaceholder.typicode.com/posts/1/comments")
                            status = "GET /posts/1/comments -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET /posts/1/comments")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Creating post..."
                        try {
                            val response = client.post("https://jsonplaceholder.typicode.com/posts") {
                                header("Content-Type", "application/json")
                                setBody("""{"title":"Wiretap Test","body":"Hello from Wiretap!","userId":1}""")
                            }
                            status = "POST /posts -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("POST /posts")
                }

                // --- Error & edge-case samples ---

                Button(onClick = {
                    scope.launch {
                        status = "Requesting 301 redirect..."
                        try {
                            val response = client.get("https://httpbin.org/redirect/1")
                            status = "GET /redirect/1 -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET 301 Redirect")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Requesting 404..."
                        try {
                            val response = client.get("https://httpbin.org/status/404")
                            status = "GET /status/404 -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET 404 Not Found")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Requesting 500..."
                        try {
                            val response = client.get("https://httpbin.org/status/500")
                            status = "GET /status/500 -> ${response.status.value}"
                        } catch (e: Exception) {
                            status = "Error: ${e.message}"
                        }
                    }
                }) {
                    Text("GET 500 Server Error")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Requesting with 1s timeout (will timeout)..."
                        try {
                            client.get("https://httpbin.org/delay/10") {
                                timeout { requestTimeoutMillis = 1000 }
                            }
                            status = "Unexpected success"
                        } catch (e: Exception) {
                            status = "Timeout: ${e.message}"
                        }
                    }
                }) {
                    Text("GET Timeout")
                }

                Button(onClick = {
                    scope.launch {
                        status = "Starting request, cancelling in 500ms..."
                        val job = launch {
                            try {
                                client.get("https://httpbin.org/delay/10") {
                                    timeout { requestTimeoutMillis = 30_000 }
                                }
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                // ignored
                            }
                        }
                        delay(500)
                        job.cancel()
                        status = "Request cancelled"
                    }
                }) {
                    Text("GET Cancel")
                }

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(onClick = { startWiretap() }) {
                    Text("Open Wiretap Console")
                }
            }
        }
    }
}
