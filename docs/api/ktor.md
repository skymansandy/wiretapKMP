# Ktor Plugin API Reference

## WiretapKtorPlugin

Top-level Ktor client plugin for HTTP request/response logging.

```kotlin
val WiretapKtorPlugin: ClientPlugin<WiretapConfig>
```

### Installation

```kotlin
HttpClient {
    install(WiretapKtorPlugin) {
        // WiretapConfig DSL
        enabled = true
        shouldLog = { url, method -> true }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.Forever
    }
}
```

### Lifecycle Hooks

| Hook | Purpose |
|------|---------|
| `onRequest` | Captures request timestamps (ms + ns) |
| `on(Send)` | Intercepts request: evaluates rules, logs request, applies mock/throttle |
| `onResponse` | Updates log entry with response data |

### Request Attributes

The plugin stores per-request state on `HttpRequestBuilder.attributes`:

| Key | Type | Description |
|-----|------|-------------|
| `WiretapRequestTimestamp` | `Long` | Request time in milliseconds |
| `WiretapRequestNanoTimestamp` | `Long` | Request time in nanoseconds |
| `WiretapMatchedRule` | `WiretapRule` | The matched rule (if any) |
| `WiretapLogEntryId` | `Long` | Database entry ID for the log |

---

## WiretapKtorWebSocketPlugin

Ktor client plugin that intercepts WebSocket upgrades (101 responses).

```kotlin
val WiretapKtorWebSocketPlugin: ClientPlugin<Unit>
```

### Installation

```kotlin
HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin)
    install(WiretapKtorPlugin)
}
```

### Behavior

- Hooks into `onResponse` for 101 Switching Protocols
- Creates a `SocketLogEntry` via the orchestrator
- Stores socket ID on request attributes for message logging

---

## WiretapWebSocketSession

Wraps a `DefaultClientWebSocketSession` to log messages.

```kotlin
class WiretapWebSocketSession(
    val delegate: DefaultClientWebSocketSession,
    // internal: socketId, orchestrator
)
```

### Factory

```kotlin
suspend fun DefaultClientWebSocketSession.wiretapWrap(): WiretapWebSocketSession
```

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `delegate` | `DefaultClientWebSocketSession` | The wrapped session |
| `incoming` | `ReceiveChannel<Frame>` | Incoming frames from delegate |

### Methods

| Method | Description |
|--------|-------------|
| `suspend fun send(frame: Frame)` | Logs the frame and sends via delegate |
| `suspend fun logReceivedFrame(frame: Frame)` | Logs a received frame (Text/Binary) |
| `suspend fun close()` | Graceful close, updates status to Closed |
| `suspend fun markFailed(error: String)` | Mark connection as Failed |
| `suspend fun markClosed(code: Short?, reason: String?)` | Mark connection as Closed |

### Auto-Close Detection

The session installs a `Job.invokeOnCompletion` handler that automatically updates the socket status when the session ends (timeout, server close, cancellation, error).

### Usage

```kotlin
client.webSocket("wss://example.com/ws") {
    val session = this.wiretapWrap()

    session.send(Frame.Text("hello"))

    for (frame in session.incoming) {
        session.logReceivedFrame(frame)
        if (frame is Frame.Text) {
            println(frame.readText())
        }
    }
}
```

---

## No-op Module (wiretap-ktor-noop)

### WiretapKtorPlugin

```kotlin
val WiretapKtorPlugin = createClientPlugin("WiretapPlugin", ::WiretapConfig) {
    // no-op
}
```

### WiretapKtorWebSocketPlugin

```kotlin
val WiretapKtorWebSocketPlugin = createClientPlugin("WiretapWebSocketPlugin") {
    // no-op
}
```

### Wiretap Object

```kotlin
object Wiretap {
    val ktorPlugin: ClientPlugin<WiretapConfig>
    val koinModule: Module  // empty
}
```
