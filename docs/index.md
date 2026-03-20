# WiretapKMP

**Kotlin Multiplatform network inspection SDK** — like Charles Proxy, but embedded in your app.

WiretapKMP intercepts HTTP and WebSocket traffic from **Ktor**, **OkHttp**, and **NSURLSession** clients, logging everything to a local SQLite database with a built-in Compose Multiplatform UI for inspection.

## Features

- **HTTP Logging** — Request/response capture with headers, bodies, timing, and TLS details
- **WebSocket Logging** — Connection lifecycle and message stream tracking
- **Mock Rules** — Return fake responses without hitting the network
- **Throttle Rules** — Add artificial delay to simulate slow connections
- **Header Masking** — Redact sensitive headers (Authorization, cookies, etc.)
- **Log Retention** — Control how long logs are kept (forever, per-session, or N days)
- **Built-in UI** — Compose Multiplatform inspector with search, filtering, and two-pane layout
- **No-op Variants** — Zero-overhead stubs for release builds

## Supported Platforms

| Client | Android | iOS | JVM Desktop |
|--------|---------|-----|-------------|
| **Ktor** | :material-check: | :material-check: | :material-check: |
| **OkHttp** | :material-check: | — | :material-check: |
| **URLSession** | — | :material-check: | — |

## Quick Example

=== "Ktor"

    ```kotlin
    val client = HttpClient {
        install(WiretapKtorPlugin) {
            logRetention = LogRetention.Days(7)
        }
    }
    ```

=== "OkHttp"

    ```kotlin
    val client = OkHttpClient.Builder()
        .addInterceptor(WiretapOkHttpInterceptor())
        .build()
    ```

=== "URLSession (Swift)"

    ```swift
    let interceptor = WiretapURLSessionInterceptor(session: .shared) {
        $0.logRetention = LogRetention.Days(days: 7)
    }
    interceptor.intercept(request: request) { data, response, error in
        // handle response
    }
    ```

## Debug vs Release

WiretapKMP ships **no-op modules** with the same API surface but zero overhead, so you can safely strip inspection from production:

```kotlin
// build.gradle.kts
debugImplementation("dev.skymansandy:wiretap-core:$version")
debugImplementation("dev.skymansandy:wiretap-ktor:$version")
releaseImplementation("dev.skymansandy:wiretap-ktor-noop:$version")
```
