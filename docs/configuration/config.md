# WiretapHttpConfig

`WiretapHttpConfig` is the shared configuration class used by both Ktor and OkHttp plugins.

## Properties

### `enabled`

**Type:** `Boolean` — **Default:** `true`

Master switch. When `false`, the plugin passes requests through without any logging, rule evaluation, or overhead.

```kotlin
install(WiretapKtorHttpPlugin) {
    enabled = BuildConfig.DEBUG
}
```

### `consoleLogging`

**Type:** `Boolean` — **Default:** `true`

Whether **this plugin** mirrors its captured traffic to the platform console — logcat on Android, the Xcode console on iOS, stdout on JVM Desktop.

```kotlin
install(WiretapKtorHttpPlugin) {
    consoleLogging = false
}
```

Set it to `false` if you already have your own HTTP logging, or if one noisy client is drowning out your logcat. Because it lives on the plugin config, each client decides for itself — a chatty polling client can stay silent while your main API client keeps printing.

| Traffic | Console output |
|---------|----------------|
| **HTTP** | `[Wiretap] GET https://api.example.com/users -> 200 (42ms) [Network] h2` |
| **WebSocket** | `[Wiretap] WS OPEN wss://…`, plus one line per frame: `[Wiretap] WS ▲ "ping" (4 B)` |
| **SSE** | `[Wiretap] SSE OPEN https://api.example.com/stream`, plus one line per event: `[Wiretap] SSE ▼ [ping] "tick" (4 B)` |

WebSocket and SSE plugins carry the same flag on their own configs — see [Ktor WebSockets](../ktor/websockets.md) and [OkHttp WebSockets](../okhttp/websockets.md).

!!! note
    This only affects console output. Every request, WebSocket frame and SSE event is still captured, stored, and browsable in the inspector UI — including notifications and search.

### `shouldLog`

**Type:** `(url: String, method: String) -> Boolean` — **Default:** `{ _, _ -> true }`

Filter which requests are captured in the log database. Evaluated before any DB write.

```kotlin
shouldLog = { url, method ->
    url.contains("/api/") && method != "OPTIONS"
}
```

!!! note
    Requests that don't pass `shouldLog` are still subject to mock/throttle rules — they just won't appear in the inspector.

### `headerAction`

**Type:** `(key: String) -> HeaderAction` — **Default:** `{ HeaderAction.Keep }`

Controls how each header is treated in logged data. Called for every header key in both requests and responses. See [Header Actions](headers.md) for details.

```kotlin
headerAction = { key ->
    when {
        key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
        key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
        else -> HeaderAction.Keep
    }
}
```

### `logRetention`

**Type:** `LogRetention` — **Default:** `LogRetention.Forever`

Controls how long log entries are retained. See [Log Retention](retention.md) for details.

```kotlin
logRetention = LogRetention.Days(7)
```

### `maxContentLength`

**Type:** `Int` — **Default:** `512000` (500 KB)

Maximum number of characters to store for request and response bodies. Bodies exceeding this limit are truncated before being saved to the database. Capped at `500 * 1024` (500 KB). Set to `0` to skip body logging entirely.

```kotlin
maxContentLength = 100 * 1024 // 100 KB
```

## Usage by Plugin

=== "Ktor"

    ```kotlin
    install(WiretapKtorHttpPlugin) {
        enabled = true
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.AppSession
        maxContentLength = 100 * 1024
    }
    ```

=== "OkHttp"

    ```kotlin
    WiretapOkHttpInterceptor {
        enabled = true
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.AppSession
        maxContentLength = 100 * 1024
    }
    ```
