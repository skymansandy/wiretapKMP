# Ktor — Setup

**Platforms:** Android, iOS, JVM Desktop

## Dependencies

```kotlin
debugImplementation("dev.skymansandy:wiretap-ktor:1.0.0-RC3")
releaseImplementation("dev.skymansandy:wiretap-ktor-noop:1.0.0-RC3")
```

For KMP shared modules:

```kotlin
sourceSets {
    commonMain {
        dependencies {
            implementation("dev.skymansandy:wiretap-ktor:1.0.0-RC3")
        }
    }
}
```

## Install the Plugin

```kotlin
val client = HttpClient {
    install(WiretapKtorPlugin) {
        enabled = true                                    // default
        shouldLog = { url, method -> true }               // default: log everything
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

## With WebSocket Support

```kotlin
val client = HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin)  // WebSocket logging
    install(WiretapKtorPlugin) {         // HTTP logging
        logRetention = LogRetention.AppSession
    }
}
```

!!! important
    Install `WiretapKtorWebSocketPlugin` **before** `WiretapKtorPlugin`. The HTTP plugin deletes 101 (Switching Protocols) entries to avoid duplicate logs.

## DI Setup Example

```kotlin
val networkModule = module {
    single {
        HttpClient {
            install(WebSockets)
            install(WiretapKtorWebSocketPlugin)
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
```

## Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Master switch — `false` disables all logging |
| `shouldLog` | `(url, method) -> Boolean` | `{ _, _ -> true }` | Filter which requests to capture |
| `headerAction` | `(key) -> HeaderAction` | `{ Keep }` | Control how headers are logged |
| `logRetention` | `LogRetention` | `Forever` | How long to keep log entries |

### `enabled`

Disable Wiretap entirely — requests pass through without any interception or overhead:

```kotlin
install(WiretapKtorPlugin) {
    enabled = BuildConfig.DEBUG
}
```

### `shouldLog`

Filter which requests appear in the inspector. Requests that don't pass the filter are still subject to mock/throttle rules — they just won't be stored in the database:

```kotlin
install(WiretapKtorPlugin) {
    shouldLog = { url, method ->
        url.contains("/api/") && method != "OPTIONS"
    }
}
```

### `headerAction`

Control how each header key is treated in logged data. The original request/response is never mutated:

```kotlin
install(WiretapKtorPlugin) {
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
install(WiretapKtorPlugin) {
    // Keep all logs indefinitely (default)
    logRetention = LogRetention.Forever

    // Clear all logs on app restart — only current session visible
    logRetention = LogRetention.AppSession

    // Auto-prune entries older than N days
    logRetention = LogRetention.Days(7)
}
```
