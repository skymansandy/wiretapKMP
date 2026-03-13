package dev.skymansandy.wiretapsample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin
import dev.skymansandy.wiretap.startWiretap
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.util.logging.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

@Composable
fun App() {
    WiretapTheme {
        val client = remember {
            HttpClient {
                install(WiretapKtorPlugin)
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

                Spacer(Modifier.height(16.dp))

                FilledTonalButton(onClick = { startWiretap() }) {
                    Text("Open Wiretap Console")
                }
            }
        }
    }
}
