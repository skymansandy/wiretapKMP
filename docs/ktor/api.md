# Ktor — API Reference

## WiretapKtorHttpPlugin

```kotlin
val WiretapKtorHttpPlugin: ClientPlugin<WiretapConfig>
```

Top-level Ktor client plugin for HTTP request/response logging with mock/throttle rule support.

### Installation

```kotlin
HttpClient {
    install(WiretapKtorHttpPlugin) {
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

## wiretapped()

```kotlin
suspend fun DefaultClientWebSocketSession.wiretapped(): WiretapWebSocketSession?
```

Extension to wrap a Ktor WebSocket session for message logging. Returns `null` if `WiretapKtorWebSocketPlugin` is not installed.

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
| `incoming` | `ReceiveChannel<Frame>` | Incoming frames with automatic logging (all frame types) |

### Methods

| Method | Description |
|--------|-------------|
| `suspend fun send(frame: Frame)` | Logs the frame and sends via delegate |
| `suspend fun close(code: Short, reason: String?)` | Logs status as Closed and closes the delegate |

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
| `WiretapKtorHttpPlugin` | Empty plugin body |
| `WiretapKtorWebSocketPlugin` | Empty plugin body |
| `wiretapModule` | Empty Koin module |

Same function signatures — zero overhead, safe to install.
