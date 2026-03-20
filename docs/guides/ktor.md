# Ktor Integration Guide

WiretapKMP provides a Ktor client plugin that intercepts all HTTP requests and responses. It supports Android, iOS, and JVM Desktop.

## Installation

Install `WiretapKtorPlugin` in your `HttpClient` configuration:

```kotlin
val client = HttpClient {
    install(WiretapKtorPlugin) {
        enabled = true  // default
        shouldLog = { url, method -> true }  // default: log everything
        logRetention = LogRetention.Days(7)
        headerAction = { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }
    }
}
```

All configuration properties are optional — with no configuration, Wiretap logs every request.

## Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Master switch — when `false`, requests pass through without logging |
| `shouldLog` | `(url, method) -> Boolean` | `{ _, _ -> true }` | Filter which requests to capture |
| `headerAction` | `(key) -> HeaderAction` | `{ Keep }` | Control how headers are logged |
| `logRetention` | `LogRetention` | `Forever` | How long to keep log entries |

## How It Works

The plugin hooks into three Ktor lifecycle points:

1. **`onRequest`** — Captures timestamps for duration measurement
2. **`on(Send)`** — Intercepts the request before it reaches the network:
    - Evaluates mock/throttle rules
    - Logs the request immediately (so it shows in the UI while in-flight)
    - If a **Mock** rule matches, returns a fake response without network access
    - If a **Throttle** rule matches, delays before proceeding
3. **`onResponse`** — Updates the log entry with response data (status, headers, body, duration)

## Request Filtering

Use `shouldLog` to control which requests are captured:

```kotlin
install(WiretapKtorPlugin) {
    shouldLog = { url, method ->
        url.contains("/api/") && method != "OPTIONS"
    }
}
```

Requests that don't pass `shouldLog` are still subject to rule evaluation (mock/throttle), but they won't appear in the log database.

## Header Masking

Protect sensitive data with `headerAction`:

```kotlin
install(WiretapKtorPlugin) {
    headerAction = { key ->
        when {
            key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
            key.equals("X-Api-Key", ignoreCase = true) -> HeaderAction.Mask("REDACTED")
            key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
            else -> HeaderAction.Keep
        }
    }
}
```

- **`Keep`** — Log the header as-is
- **`Mask(mask = "***")`** — Replace the value with the mask string
- **`Skip`** — Omit the header entirely from logs

!!! info
    Header actions only affect logged data. The original request/response headers are never mutated.

## Log Retention

```kotlin
install(WiretapKtorPlugin) {
    logRetention = LogRetention.Forever      // Keep all logs (default)
    logRetention = LogRetention.AppSession   // Clear on app restart
    logRetention = LogRetention.Days(7)      // Prune entries older than 7 days
}
```

- **`AppSession`** clears all logs when the plugin initializes (first request)
- **`Days(n)`** prunes old entries on each new request capture (uses indexed queries)

## Mock Rules

When a request matches a mock rule, Wiretap returns a fake response without hitting the network:

```kotlin
// Rules are managed via WiretapDi.ruleRepository or the built-in UI
val rule = WiretapRule(
    method = "GET",
    urlMatcher = UrlMatcher.Contains("/api/users"),
    action = RuleAction.Mock(
        responseCode = 200,
        responseBody = """{"users": []}""",
        responseHeaders = mapOf("Content-Type" to "application/json"),
    ),
)
```

Mock responses appear in the inspector with a **Mock** badge and zero network duration.

## Throttle Rules

Throttle rules add artificial delay before proceeding to the real network:

```kotlin
val rule = WiretapRule(
    urlMatcher = UrlMatcher.Contains("/api/"),
    action = RuleAction.Throttle(
        delayMs = 2000,                // Fixed 2-second delay
        // delayMaxMs = 5000,          // Or random between 2-5 seconds
    ),
)
```

Throttled responses appear with a **Throttle** badge and include the extra delay in the duration.

## Error Handling

Wiretap logs exceptions and re-throws them so your error handling works normally:

- **Network errors** — Logged with response code `0` and the error message as the body
- **Cancelled requests** — Logged with response code `-1`
- **In-progress requests** — Logged with response code `-2` (visible in the UI as "pending")

## Debug vs Release

```kotlin
// build.gradle.kts
debugImplementation("dev.skymansandy:wiretap-core:$version")
debugImplementation("dev.skymansandy:wiretap-ktor:$version")
releaseImplementation("dev.skymansandy:wiretap-ktor-noop:$version")
```

The no-op module provides the same `WiretapKtorPlugin` val with an empty body — safe to install with zero overhead.

## Complete Example

```kotlin
// DI module
val networkModule = module {
    single {
        HttpClient {
            install(WebSockets)
            install(WiretapKtorWebSocketPlugin)  // Optional: WebSocket logging
            install(WiretapKtorPlugin) {
                logRetention = LogRetention.AppSession
                headerAction = { key ->
                    if (key.equals("Authorization", ignoreCase = true))
                        HeaderAction.Mask()
                    else
                        HeaderAction.Keep
                }
            }
        }
    }
}

// Usage
class ApiClient(private val client: HttpClient) {
    suspend fun getUsers(): List<User> {
        return client.get("https://api.example.com/users").body()
    }
}
```
