# WebSocket Support

WiretapKMP logs WebSocket connections and messages for both Ktor and OkHttp clients.

## Ktor WebSockets

### Setup

Install both the standard WebSocket plugin and the Wiretap WebSocket plugin:

```kotlin
val client = HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin)  // Logs connections
    install(WiretapKtorPlugin)           // Logs HTTP (deletes 101 upgrade entries)
}
```

!!! important
    Install `WiretapKtorWebSocketPlugin` **before** `WiretapKtorPlugin`. The HTTP plugin deletes 101 (Switching Protocols) entries to avoid duplicate logs.

### Session Wrapping

Wrap your WebSocket session with `wiretapWrap()` to log outgoing and incoming messages:

```kotlin
client.webSocket("wss://echo.websocket.org") {
    val session = this.wiretapWrap()

    // Send — automatically logged
    session.send(Frame.Text("Hello, server!"))

    // Receive — call logReceivedFrame() to log
    for (frame in session.incoming) {
        session.logReceivedFrame(frame)
        when (frame) {
            is Frame.Text -> println("Received: ${frame.readText()}")
            is Frame.Binary -> println("Received ${frame.readBytes().size} bytes")
            else -> {}
        }
    }
}
```

### WiretapWebSocketSession API

| Method | Description |
|--------|-------------|
| `send(frame)` | Logs the frame and sends it via the delegate |
| `logReceivedFrame(frame)` | Logs a received frame (Text or Binary) |
| `close()` | Gracefully closes and updates status to Closed |
| `markFailed(error)` | Manually mark connection as Failed |
| `markClosed(code, reason)` | Manually mark connection as Closed |
| `incoming` | Direct access to the delegate's incoming channel |

### Auto-Close Detection

`WiretapWebSocketSession` automatically detects when the session ends (timeout, server close, error) via `Job.invokeOnCompletion`. You don't need to call `markClosed()` or `markFailed()` manually — the status is updated automatically.

---

## OkHttp WebSockets

### Setup

Wrap your `WebSocketListener` with `WiretapOkHttpWebSocketListener`:

```kotlin
val myListener = object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("Hello!")  // Automatically logged (outgoing)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Received: $text")  // Automatically logged (incoming)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("Closed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Failed: ${t.message}")
    }
}

val listener = WiretapOkHttpWebSocketListener(myListener)
val request = Request.Builder().url("wss://echo.websocket.org").build()
client.newWebSocket(request, listener)
```

### How It Works

The wrapper intercepts all `WebSocketListener` callbacks:

| Callback | What's Logged |
|----------|--------------|
| `onOpen` | Connection opened (status: Open). WebSocket is wrapped for outgoing message logging |
| `onMessage(text)` | Text message received |
| `onMessage(bytes)` | Binary message received (logged as `[Binary: N bytes]`) |
| `onClosing` | Connection status updated to Closing with close code/reason |
| `onClosed` | Connection status updated to Closed with timestamp |
| `onFailure` | Connection status updated to Failed with error message |

**Outgoing messages** are logged automatically because `onOpen` receives a `WiretapWebSocket` that intercepts `send()` calls.

---

## What's Logged

### Connection Entry

Each WebSocket connection creates a `SocketLogEntry`:

- URL (converted to `ws://` or `wss://`)
- Request headers
- Connection status (Connecting → Open → Closing → Closed/Failed)
- Close code and reason
- Failure message (if applicable)
- Protocol version
- Message count

### Messages

Each sent/received frame creates a `SocketMessage`:

- Socket ID (links to connection)
- Direction (Sent / Received)
- Content type (Text / Binary)
- Content (text or `[Binary: N bytes]`)
- Byte count
- Timestamp

### Inspector UI

The Wiretap inspector shows:

- **WebSocket tab** — List of all connections with status indicators
- **Connection detail** — Connection metadata, close info, and the full message stream
- **Message stream** — Chronological list with direction arrows (sent/received)
