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

## Automatic Session Wrapping

`WiretapKtorWebSocketPlugin` wraps WebSocket sessions automatically — no extra calls needed. All sent and received frames are logged:

```kotlin
client.webSocket("wss://echo.websocket.org") {
    // Send — automatically logged
    send(Frame.Text("Hello, server!"))

    // Receive — automatically logged as frames are consumed
    for (frame in incoming) {
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
4. The plugin automatically wraps the session to intercept `send()` and auto-log `incoming` frames
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
- Content (text string, decoded binary or `[Binary: N bytes]`, or close code/reason)
- Byte count
- Timestamp

## Configuration

```kotlin
install(WiretapKtorWebSocketPlugin) {
    enabled = BuildConfig.DEBUG

    // How to render Binary frames. Defaults to Auto.
    binaryDecoding = BinaryFrameDecoding.Auto
    // = BinaryFrameDecoding.Utf8         // always decode as UTF-8 (replacement chars on invalid bytes)
    // = BinaryFrameDecoding.Placeholder  // never decode, always show "[Binary: N bytes]"
    // = BinaryFrameDecoding.Custom { bytes -> bytes.toHexPreview() }
}
```

### `binaryDecoding`

`Auto` (default) tries strict UTF-8 — if the payload is valid printable text (tab/LF/CR plus the SignalR Core record separator `0x1E` are tolerated, matching what the JSON hub protocol can carry as raw bytes) it's shown as text, otherwise it falls back to `[Binary: N bytes]`. This lets libraries that ship text-over-binary (e.g. SignalRKore) appear readable without misrepresenting genuine binary like protobuf or MessagePack.

Use `Custom` for non-UTF-8 charsets, hex previews, or pretty-printing. The decoder is held on the `HttpClient` for its lifetime — prefer a top-level function or object method reference over a lambda that captures `Activity` / composable scope.
