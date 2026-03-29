# OkHttp — WebSocket Logging

=== "Connections"

    ![WebSocket List](../assets/screenshots/socket/socketlist.png){ width="300" }

=== "Messages"

    ![WebSocket Detail](../assets/screenshots/socket/socketdetail.png){ width="300" }

## Setup

### Extension function (recommended)

Use the `wiretapped()` extension on any `WebSocketListener`:

```kotlin
val request = Request.Builder().url("wss://echo.websocket.org").build()
client.newWebSocket(request, myListener.wiretapped())
```

### Constructor

Alternatively, wrap your listener using the constructor directly:

```kotlin
val listener = WiretapOkHttpWebSocketListener(myListener)
val request = Request.Builder().url("wss://echo.websocket.org").build()
client.newWebSocket(request, listener)
```

### Full example

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

val request = Request.Builder().url("wss://echo.websocket.org").build()
client.newWebSocket(request, myListener.wiretapped())
```

## How It Works

`WiretapOkHttpWebSocketListener` wraps all `WebSocketListener` callbacks:

| Callback | What's Logged |
|----------|--------------|
| `onOpen` | Connection opened (status: Open). WebSocket is wrapped for outgoing logging |
| `onMessage(text)` | Text message received |
| `onMessage(bytes)` | Binary message received (logged as `[Binary: N bytes]`) |
| `onClosing` | Status updated to Closing with close code/reason |
| `onClosed` | Status updated to Closed with timestamp |
| `onFailure` | Status updated to Failed with error message |

All events are delegated to your original listener after logging.

### Outgoing Message Logging

The `webSocket` parameter passed to your `onOpen()` callback is actually a `WiretapWebSocket` that intercepts `send()` calls:

```kotlin
override fun onOpen(webSocket: WebSocket, response: Response) {
    // webSocket is a WiretapWebSocket — send() calls are logged automatically
    webSocket.send("Hello!")        // Logged: Text, Sent, "Hello!"
    webSocket.send(byteString)      // Logged: Binary, Sent, "[Binary: N bytes]"
}
```

All other `WebSocket` methods (`close()`, `cancel()`, `request()`, `queueSize()`) pass through to the original.

## What Gets Logged

### Connection

- URL
- Request headers
- Protocol
- Status transitions (Open → Closing → Closed / Failed)
- Close code and reason
- Failure message

### Messages

- Direction (Sent / Received)
- Content type (Text / Binary)
- Content (text or `[Binary: N bytes]`)
- Byte count
- Timestamp
