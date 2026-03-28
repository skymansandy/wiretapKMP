# WiretapConfig

`WiretapConfig` is the shared configuration class used by both Ktor and OkHttp plugins.

## Properties

### `enabled`

**Type:** `Boolean` — **Default:** `true`

Master switch. When `false`, the plugin passes requests through without any logging, rule evaluation, or overhead.

```kotlin
install(WiretapKtorHttpPlugin) {
    enabled = BuildConfig.DEBUG
}
```

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

## Usage by Plugin

=== "Ktor"

    ```kotlin
    install(WiretapKtorHttpPlugin) {
        enabled = true
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.AppSession
    }
    ```

=== "OkHttp"

    ```kotlin
    WiretapOkHttpInterceptor {
        enabled = true
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.AppSession
    }
    ```
