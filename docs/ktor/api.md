# Ktor — API Reference

## WiretapKtorHttpPlugin

```kotlin
val WiretapKtorHttpPlugin: ClientPlugin<WiretapHttpConfig>
```

Top-level Ktor client plugin for HTTP request/response logging with mock/throttle rule support.

### Installation

```kotlin
HttpClient {
    install(WiretapKtorHttpPlugin) {
        // WiretapHttpConfig DSL — all properties optional
        enabled = true
        shouldLog = { url, method -> true }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.Forever
        maxContentLength = 100 * 1024
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

## WiretapHttpConfig

```kotlin
class WiretapHttpConfig {
    var enabled: Boolean = true
    var shouldLog: (url: String, method: String) -> Boolean = { _, _ -> true }
    var headerAction: (key: String) -> HeaderAction = { HeaderAction.Keep }
    var logRetention: LogRetention = LogRetention.Forever
    var maxContentLength: Int = MAX_CONTENT_LENGTH  // 500 * 1024
}
```

---

## WiretapKtorSsePlugin

```kotlin
val WiretapKtorSsePlugin: ClientPlugin<Unit>
```

Placeholder plugin for SSE inspection. SSE connection tracking is handled by the `wiretapped()` extension on `ClientSSESession`.

---

## wiretapped() (SSE)

```kotlin
suspend fun ClientSSESession.wiretapped(): WiretapSseSession
```

Extension to wrap a Ktor SSE session for event logging. Creates a connection entry in Wiretap and returns a `WiretapSseSession` that intercepts incoming events.

---

## WiretapSseSession

```kotlin
interface WiretapSseSession {
    val call: HttpClientCall
    val incoming: Flow<ServerSentEvent>
}
```

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `call` | `HttpClientCall` | The underlying HTTP call for this SSE connection |
| `incoming` | `Flow<ServerSentEvent>` | Incoming events with automatic logging |

---

## No-op (wiretap-ktor-noop)

| Component | Behavior |
|-----------|----------|
| `WiretapKtorHttpPlugin` | Empty plugin body |
| `WiretapKtorWebSocketPlugin` | Empty plugin body |
| `WiretapKtorSsePlugin` | Empty plugin body |
| `wiretapModule` | Empty Koin module |

Same function signatures — zero overhead, safe to install.
