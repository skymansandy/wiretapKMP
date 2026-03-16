package dev.skymansandy.wiretapsample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

// Same color palette used in wiretap-core NetworkLogItem for HTTP status codes
private val ColorSuccess = Color(0xFF4CAF50) // Green – 2xx
private val ColorRedirect = Color(0xFF42A5F5) // Blue – 3xx
private val ColorClientError = Color(0xFFFFA726) // Amber – 4xx
private val ColorServerError = Color(0xFFEF5350) // Red – 5xx
private val ColorEdgeCase = Color(0xFF9E9E9E) // Gray – timeout / cancel

private enum class ActionCategory { SUCCESS, REDIRECT, CLIENT_ERROR, SERVER_ERROR, EDGE_CASE }

private data class ApiAction(
    val label: String,
    val category: ActionCategory,
    val action: suspend (HttpClient, (String) -> Unit) -> Unit,
)

private val actionColor = mapOf(
    ActionCategory.SUCCESS to ColorSuccess,
    ActionCategory.REDIRECT to ColorRedirect,
    ActionCategory.CLIENT_ERROR to ColorClientError,
    ActionCategory.SERVER_ERROR to ColorServerError,
    ActionCategory.EDGE_CASE to ColorEdgeCase,
)

private val apiActions = listOf(
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
            timeout { requestTimeoutMillis = 1000 }
        }
        onStatus("Unexpected success")
    },
    ApiAction("Cancel", ActionCategory.EDGE_CASE) { client, onStatus ->
        onStatus("Cancelling in 500ms ...")
    },
)

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
            CoroutineExceptionHandler { _, _ -> }
        }
        var statusLog by remember { mutableStateOf("Ready. Tap a button to make a request.") }

        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Title
                Text(
                    text = "Wiretap Sample",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                // Status window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp),
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = statusLog,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // Button grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(apiActions) { action ->
                        val color = actionColor.getValue(action.category)
                        Button(
                            onClick = {
                                scope.launch {
                                    if (action.label == "Cancel") {
                                        statusLog = "Starting request, cancelling in 500ms..."
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
                                        statusLog = "Request cancelled"
                                    } else {
                                        try {
                                            action.action(client) { statusLog = it }
                                        } catch (e: Exception) {
                                            statusLog = "Error: ${e.message}"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = color,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Open Wiretap Console button
                FilledTonalButton(
                    onClick = { startWiretap() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open Wiretap Console")
                }
            }
        }
    }
}
