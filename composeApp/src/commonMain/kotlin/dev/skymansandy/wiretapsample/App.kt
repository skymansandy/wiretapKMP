package dev.skymansandy.wiretapsample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.helper.notification.enableLaunchTool
import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin
import dev.skymansandy.wiretap.plugin.WiretapKtorWebSocketPlugin
import dev.skymansandy.wiretap.plugin.WiretapWebSocketSession
import dev.skymansandy.wiretap.plugin.wiretapWrap
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Same color palette used in wiretap-core NetworkLogItem for HTTP status codes
private val ColorSuccess = Color(0xFF4CAF50)
private val ColorRedirect = Color(0xFF42A5F5)
private val ColorClientError = Color(0xFFFFA726)
private val ColorServerError = Color(0xFFEF5350)
private val ColorEdgeCase = Color(0xFF9E9E9E)

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

private val httpActions = listOf(
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

private val wsServers = listOf(
    "wss://echo.websocket.org" to "echo.websocket.org",
    "wss://ws.postman-echo.com/raw" to "Postman Echo",
)

@Composable
fun App() {
    LaunchedEffect(Unit) { enableLaunchTool() }
    WiretapTheme {
        val client = remember {
            HttpClient {
                install(WebSockets)
                install(WiretapKtorPlugin)
                install(WiretapKtorWebSocketPlugin)
                install(HttpTimeout)
            }
        }
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text("HTTP") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Stream, contentDescription = null) },
                        label = { Text("WebSocket") },
                    )
                }
            },
        ) { padding ->
            when (selectedTab) {
                0 -> HttpTab(client = client, modifier = Modifier.padding(padding))
                1 -> WebSocketTab(client = client, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HttpTab(client: HttpClient, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope {
        CoroutineExceptionHandler { _, _ -> }
    }
    var statusLog by remember { mutableStateOf("Ready. Tap a button to make a request.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "HTTP Requests",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        StatusWindow(statusLog)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(httpActions) { action ->
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
    }
}

private data class WsLogEntry(val direction: String, val text: String)

@Composable
private fun WebSocketTab(client: HttpClient, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope {
        CoroutineExceptionHandler { _, _ -> }
    }
    var isConnected by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }
    var selectedServerIndex by remember { mutableIntStateOf(0) }
    var wsUrl by remember { mutableStateOf(wsServers[0].first) }
    var session by remember { mutableStateOf<WiretapWebSocketSession?>(null) }
    var connectionJob by remember { mutableStateOf<Job?>(null) }
    var messageText by remember { mutableStateOf("") }
    val messageLog = remember { mutableStateListOf<WsLogEntry>() }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messageLog.size) {
        if (messageLog.isNotEmpty()) {
            listState.animateScrollToItem(messageLog.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "WebSocket",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // Server selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            wsServers.forEachIndexed { index, (url, label) ->
                OutlinedButton(
                    onClick = {
                        if (!isConnected && !isConnecting) {
                            selectedServerIndex = index
                            wsUrl = url
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedServerIndex == index)
                            MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                    ),
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }

        // Connect / Disconnect
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    if (isConnected) {
                        // Disconnect
                        scope.launch {
                            try {
                                session?.markClosed(1000, "User disconnected")
                                session?.delegate?.close()
                            } catch (_: Exception) {}
                            connectionJob?.cancel()
                            session = null
                            isConnected = false
                            messageLog.add(WsLogEntry("SYS", "Disconnected"))
                        }
                    } else if (!isConnecting) {
                        // Connect
                        isConnecting = true
                        messageLog.clear()
                        messageLog.add(WsLogEntry("SYS", "Connecting to $wsUrl ..."))
                        connectionJob = scope.launch {
                            try {
                                client.webSocket(wsUrl) {
                                    val wrapped = this.wiretapWrap()
                                    session = wrapped
                                    isConnected = true
                                    isConnecting = false
                                    messageLog.add(WsLogEntry("SYS", "Connected!"))

                                    // Listen for incoming messages
                                    try {
                                        for (frame in wrapped.incoming) {
                                            if (frame is Frame.Text) {
                                                val text = frame.readText()
                                                wrapped.logReceivedFrame(frame)
                                                messageLog.add(WsLogEntry("RECV", text))
                                            }
                                        }
                                    } catch (_: Exception) {}
                                    // Session ended
                                    isConnected = false
                                    session = null
                                    messageLog.add(WsLogEntry("SYS", "Connection closed"))
                                }
                            } catch (e: Exception) {
                                isConnecting = false
                                isConnected = false
                                session = null
                                messageLog.add(WsLogEntry("SYS", "Error: ${e.message}"))
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) ColorServerError else ColorSuccess,
                ),
                enabled = !isConnecting,
            ) {
                Text(
                    text = when {
                        isConnecting -> "Connecting..."
                        isConnected -> "Disconnect"
                        else -> "Connect"
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            if (isConnected) {
                Text(
                    text = "Connected",
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorSuccess,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Message log
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(messageLog) { entry ->
                WsMessageItem(entry)
            }
        }

        // Send message input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                singleLine = true,
                enabled = isConnected,
            )
            IconButton(
                onClick = {
                    val text = messageText.trim()
                    if (text.isNotEmpty() && isConnected) {
                        val currentSession = session
                        messageText = ""
                        scope.launch {
                            try {
                                currentSession?.send(Frame.Text(text))
                                messageLog.add(WsLogEntry("SENT", text))
                            } catch (e: Exception) {
                                messageLog.add(WsLogEntry("SYS", "Send failed: ${e.message}"))
                            }
                        }
                    }
                },
                enabled = isConnected && messageText.isNotBlank(),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (isConnected && messageText.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            }
        }
    }
}

@Composable
private fun WsMessageItem(entry: WsLogEntry) {
    val (bgColor, textColor, alignment) = when (entry.direction) {
        "SENT" -> Triple(
            Color(0xFF7E57C2).copy(alpha = 0.15f),
            Color(0xFF7E57C2),
            Alignment.CenterEnd,
        )
        "RECV" -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            Alignment.CenterStart,
        )
        else -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.Center,
        )
    }

    if (entry.direction == "SYS") {
        Text(
            text = entry.text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        )
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            contentAlignment = alignment,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = if (entry.direction == "SENT") "\u2191 " else "\u2193 ",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
private fun StatusWindow(statusLog: String) {
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
}
