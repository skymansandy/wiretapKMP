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

Intercepts WebSocket upgrades (101 responses) and automatically wraps sessions to log all sent and received frames. No extra calls needed — just use the session directly.

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

SSE plugin that automatically wraps SSE sessions to log all incoming events. No extra calls needed — just use the session directly.

---

## No-op (wiretap-ktor-noop)

| Component | Behavior |
|-----------|----------|
| `WiretapKtorHttpPlugin` | Empty plugin body |
| `WiretapKtorWebSocketPlugin` | Empty plugin body |
| `WiretapKtorSsePlugin` | Empty plugin body |
| `wiretapModule` | Empty Koin module |

Same function signatures — zero overhead, safe to install.
