# Getting Started

WiretapKMP is a network inspection SDK for Kotlin Multiplatform and Swift. It captures HTTP and WebSocket traffic from your app, stores it locally, and gives you a built-in UI to browse requests, responses, and WebSocket messages — no proxy server needed.

## How It Works

Wiretap sits between your app code and your HTTP client. When you make a network request, Wiretap captures it, logs it to a local SQLite database, and forwards it to the actual network. When the response comes back, it captures that too.

```
Your App  →  Wiretap Plugin  →  HTTP Client  →  Network
                  ↓
            Local SQLite DB
                  ↓
           Built-in Inspector UI
```

You interact with your HTTP client exactly as before — Wiretap works transparently in the background. You can then open the inspector UI at any time to browse captured traffic, search logs, create mock rules, or simulate slow responses.

## Pick Your Plugin

Wiretap has a plugin for each major HTTP client. Pick the one that matches your stack:

| Your HTTP Client | Wiretap Plugin | Platforms |
|------------------|---------------|-----------|
| **Ktor** | `wiretap-ktor` | Android, iOS, JVM Desktop |
| **OkHttp** | `wiretap-okhttp` | Android, JVM Desktop |
| **URLSession** (Swift) | `wiretap-urlsession` | iOS |

### Ktor (KMP / Android / JVM)

Add the dependency:

```kotlin
// Debug only — use wiretap-ktor-noop for release builds
debugImplementation("dev.skymansandy:wiretap-ktor:1.0.0-RC6")
releaseImplementation("dev.skymansandy:wiretap-ktor-noop:1.0.0-RC6")
```

Install the plugin on your HttpClient:

```kotlin
val client = HttpClient {
    install(WiretapKtorHttpPlugin)
}
```

That's it — all HTTP requests through this client are now captured.

[:material-arrow-right: Full Ktor setup guide](ktor/setup.md)

### OkHttp (Android / JVM)

Add the dependency:

```kotlin
debugImplementation("dev.skymansandy:wiretap-okhttp:1.0.0-RC6")
releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:1.0.0-RC6")
```

Add the interceptor to your OkHttpClient:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor())
    .build()
```

[:material-arrow-right: Full OkHttp setup guide](okhttp/setup.md)

### URLSession (Swift / iOS)

Add the WiretapKMP SPM package to your project, then use `WiretapURLSession` instead of `URLSession`:

```swift
#if DEBUG
import WiretapURLSession
let session = WiretapURLSession(configuration: .default)
#else
let session = URLSession.shared
#endif
```

[:material-arrow-right: Full URLSession setup guide](urlsession/setup.md)

## Open the Inspector

Wiretap includes a built-in shake-to-open inspector. Call this once at app startup:

=== "Kotlin (Android / JVM)"

    ```kotlin
    enableWiretapLauncher()
    ```

=== "Swift (iOS)"

    ```swift
    WiretapLauncher_iosKt.enableLaunchTool()
    ```

| Platform | Trigger |
|----------|---------|
| **Android** | Shake your device |
| **iOS** | Shake your device |
| **JVM Desktop** | `Ctrl+Shift+D` |

## What You Can Do

Once Wiretap is set up, you can:

- **Browse HTTP logs** — see every request and response with headers, bodies, status codes, and timing
- **Inspect WebSocket connections** — view connection lifecycle and every message sent/received
- **Mock API responses** — create rules that return fake responses without hitting the network
- **Throttle requests** — add artificial delay to simulate slow connections
- **Mask sensitive headers** — redact Authorization, Cookie, or any header from logs
- **Control log retention** — keep logs forever, per session, or auto-prune after N days

## Plugin-Specific Documentation

Each plugin has detailed documentation covering configuration, WebSocket support, mock/throttle rules, and more:

- [Ktor Plugin](ktor/setup.md) — HTTP plugin, WebSocket plugin, configuration reference
- [OkHttp Interceptor](okhttp/setup.md) — HTTP interceptor, WebSocket listener, configuration reference
- [URLSession Wrapper](urlsession/setup.md) — WiretapURLSession, SPM setup, Swift type bridging
