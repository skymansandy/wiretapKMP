# Ktor — WebSocket Logging

## Setup

Install both the standard WebSocket plugin and the Wiretap WebSocket plugin:

```kotlin
val client = HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin)  // Logs connections
    install(WiretapKtorPlugin)           // Logs HTTP (deletes 101 upgrade entries)
}
```

## Session Wrapping

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

## WiretapWebSocketSession API

| Method | Description |
|--------|-------------|
| `send(frame)` | Logs the frame and sends via delegate |
| `logReceivedFrame(frame)` | Logs a received frame (Text or Binary) |
| `close()` | Graceful close, updates status to Closed |
| `markFailed(error)` | Manually mark connection as Failed |
| `markClosed(code, reason)` | Manually mark connection as Closed |
| `incoming` | Direct access to the delegate's incoming channel |

## Auto-Close Detection

`WiretapWebSocketSession` installs a `Job.invokeOnCompletion` handler that automatically updates the socket status when the session ends — whether from timeout, server close, cancellation, or error. You don't need to call `markClosed()` or `markFailed()` manually.

## How It Works

1. **`WiretapKtorWebSocketPlugin`** hooks into `onResponse` for 101 Switching Protocols responses
2. Creates a `SocketLogEntry` via the orchestrator with status `Open`
3. Stores the socket ID on request attributes
4. **`wiretapWrap()`** creates a `WiretapWebSocketSession` that intercepts `send()` and provides `logReceivedFrame()`
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
- Content type (Text / Binary)
- Content (text string or `[Binary: N bytes]`)
- Byte count
- Timestamp
