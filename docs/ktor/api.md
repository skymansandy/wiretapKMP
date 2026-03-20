# Ktor — API Reference

## WiretapKtorPlugin

```kotlin
val WiretapKtorPlugin: ClientPlugin<WiretapConfig>
```

Top-level Ktor client plugin for HTTP request/response logging with mock/throttle rule support.

### Installation

```kotlin
HttpClient {
    install(WiretapKtorPlugin) {
        // WiretapConfig DSL — all properties optional
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

---

## WiretapKtorWebSocketPlugin

```kotlin
val WiretapKtorWebSocketPlugin: ClientPlugin<Unit>
```

Intercepts WebSocket upgrades (101 responses) to log connections.

---

## wiretapWrap()

```kotlin
suspend fun DefaultClientWebSocketSession.wiretapWrap(): WiretapWebSocketSession
```

Extension to wrap a Ktor WebSocket session for message logging.

---

## WiretapWebSocketSession

```kotlin
class WiretapWebSocketSession(
    val delegate: DefaultClientWebSocketSession,
)
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
| `suspend fun close()` | Graceful close, updates status |
| `suspend fun markFailed(error: String)` | Mark connection as Failed |
| `suspend fun markClosed(code: Short?, reason: String?)` | Mark connection as Closed |

---

## WiretapConfig

```kotlin
class WiretapConfig {
    var enabled: Boolean = true
    var shouldLog: (url: String, method: String) -> Boolean = { _, _ -> true }
    var headerAction: (key: String) -> HeaderAction = { HeaderAction.Keep }
    var logRetention: LogRetention = LogRetention.Forever
}
```

---

## No-op (wiretap-ktor-noop)

| Component | Behavior |
|-----------|----------|
| `WiretapKtorPlugin` | Empty plugin body |
| `WiretapKtorWebSocketPlugin` | Empty plugin body |
| `wiretapModule` | Empty Koin module |

Same function signatures — zero overhead, safe to install.
