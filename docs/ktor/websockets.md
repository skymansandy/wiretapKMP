# Ktor — WebSocket Logging

=== "Connections"

    ![WebSocket List](../assets/screenshots/socket/socketlist.png){ width="300" }

=== "Messages"

    ![WebSocket Detail](../assets/screenshots/socket/socketdetail.png){ width="300" }

## Setup

Install both the standard WebSocket plugin and the Wiretap WebSocket plugin:

```kotlin
val client = HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin)  // Logs connections
    install(WiretapKtorHttpPlugin)           // Logs HTTP (deletes 101 upgrade entries)
}
```

## Session Wrapping

Wrap your WebSocket session with `wiretapped()` to log outgoing and incoming messages. Returns `null` if `WiretapKtorWebSocketPlugin` is not installed:

```kotlin
client.webSocket("wss://echo.websocket.org") {
    val session = this.wiretapped() // null if plugin not installed

    // Send — automatically logged when session is available
    session?.send(Frame.Text("Hello, server!"))

    // Receive — automatically logged as frames are consumed
    for (frame in (session?.incoming ?: incoming)) {
        when (frame) {
            is Frame.Text -> println("Received: ${frame.readText()}")
            is Frame.Binary -> println("Received ${frame.readBytes().size} bytes")
            else -> {}
        }
    }
}
```

## WiretapWebSocketSession API

| Method | Description |
|--------|-------------|
| `send(frame)` | Logs the frame and sends via delegate |
| `close(code, reason)` | Graceful close, logs status as Closed and closes the delegate |
| `incoming` | Incoming frames channel with automatic logging (all frame types) |

## Auto-Close Detection

`WiretapWebSocketSession` installs a `Job.invokeOnCompletion` handler that automatically updates the socket status when the session ends — whether from timeout, server close, cancellation, or error.

## How It Works

1. **`WiretapKtorWebSocketPlugin`** hooks into `onResponse` for 101 Switching Protocols responses
2. Creates a `SocketEntry` via the orchestrator with status `Open`
3. Stores the socket ID on request attributes
4. **`wiretapped()`** creates a `WiretapWebSocketSession` that intercepts `send()` and auto-logs `incoming` frames
5. Connection close/failure is detected automatically via job completion

## What Gets Logged

### Connection

- URL (converted to `ws://` / `wss://`)
- Request headers
- Status transitions (Open → Closing → Closed / Failed)
- Close code and reason
- Protocol version

### Messages

- Direction (Sent / Received)
- Content type (Text / Binary / Ping / Pong / Close)
- Content (text string, `[Binary: N bytes]`, or close code/reason)
- Byte count
- Timestamp
