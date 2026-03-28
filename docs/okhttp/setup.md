# OkHttp — Setup

**Platforms:** Android, JVM Desktop

## Dependencies

```kotlin
debugImplementation("dev.skymansandy:wiretap-okhttp:1.0.0-RC3")
releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:1.0.0-RC3")
```

## Install the Interceptor

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        enabled = true                                    // default
        shouldLog = { url, method -> true }               // default: log everything
        logRetention = LogRetention.Days(7)
        maxContentLength = 100 * 1024                     // truncate bodies > 100 KB
        headerAction = { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }
    })
    .build()
```

All configuration properties are optional — with no arguments, Wiretap logs every request.

## With WebSocket Support

Use the `wiretapped()` extension to wrap your listener:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor())
    .build()

client.newWebSocket(request, myWebSocketListener.wiretapped())
```

Or use the constructor directly:

```kotlin
val listener = WiretapOkHttpWebSocketListener(myWebSocketListener)
client.newWebSocket(request, listener)
```

## DI Setup Example

```kotlin
val okHttpModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(WiretapOkHttpInterceptor {
                logRetention = LogRetention.AppSession
                headerAction = { key ->
                    if (key.equals("Authorization", ignoreCase = true))
                        HeaderAction.Mask()
                    else
                        HeaderAction.Keep
                }
            })
            .build()
    }
}
```

## Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Master switch — `false` disables all logging |
| `shouldLog` | `(url, method) -> Boolean` | `{ _, _ -> true }` | Filter which requests to capture |
| `headerAction` | `(key) -> HeaderAction` | `{ Keep }` | Control how headers are logged |
| `logRetention` | `LogRetention` | `Forever` | How long to keep log entries |
| `maxContentLength` | `Int` | `512000` (500 KB) | Max characters for request/response bodies. `0` disables body logging. |

### `enabled`

Disable Wiretap entirely — requests pass through without any interception or overhead:

```kotlin
WiretapOkHttpInterceptor {
    enabled = BuildConfig.DEBUG
}
```

### `shouldLog`

Filter which requests appear in the inspector. Requests that don't pass the filter are still subject to mock/throttle rules — they just won't be stored in the database:

```kotlin
WiretapOkHttpInterceptor {
    shouldLog = { url, method ->
        url.contains("/api/") && method != "OPTIONS"
    }
}
```

### `headerAction`

Control how each header key is treated in logged data. The original request/response is never mutated:

```kotlin
WiretapOkHttpInterceptor {
    headerAction = { key ->
        when {
            // Replace value with "***"
            key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()

            // Replace value with custom mask
            key.equals("X-Api-Key", ignoreCase = true) -> HeaderAction.Mask("REDACTED")

            // Omit header from logs entirely
            key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip

            // Log as-is (default)
            else -> HeaderAction.Keep
        }
    }
}
```

### `logRetention`

Control how long log entries are retained in the SQLite database:

```kotlin
WiretapOkHttpInterceptor {
    // Keep all logs indefinitely (default)
    logRetention = LogRetention.Forever

    // Clear all logs on app restart — only current session visible
    logRetention = LogRetention.AppSession

    // Auto-prune entries older than N days
    logRetention = LogRetention.Days(7)
}
```

### `maxContentLength`

Control the maximum number of characters stored for request and response bodies. Bodies exceeding this limit are truncated before being saved to the database. Capped at 500 KB. Set to `0` to skip body logging entirely:

```kotlin
WiretapOkHttpInterceptor {
    maxContentLength = 100 * 1024  // 100 KB
}
```
