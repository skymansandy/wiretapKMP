# wiretap-ktor

Ktor client plugin for WiretapKMP. Intercepts HTTP requests and WebSocket connections, logging them to the Wiretap database for inspection via the built-in Compose UI.

**Platforms:** Android, iOS, JVM

## 📦 Setup

### Kotlin (KMP / Android)

```kotlin
// build.gradle.kts
dependencies {
    // Debug builds — full inspection
    debugImplementation("dev.skymansandy:wiretap-ktor:<version>")

    // Release builds — zero-overhead no-op stubs
    releaseImplementation("dev.skymansandy:wiretap-ktor-noop:<version>")
}
```


---

## 📡 HTTP Plugin (`WiretapKtorHttpPlugin`)

### ⚙️ How It Works

`WiretapKtorHttpPlugin` is a standard Ktor client plugin installed via `HttpClient { install(...) }`. It hooks into Ktor's request/response pipeline:

1. **On request** — Captures URL, method, headers, and body. Checks for matching mock/throttle rules. Logs the request to the Room database.
2. **Rule evaluation** — If a **mock rule** matches, returns a fake response immediately without hitting the network. If a **throttle rule** matches, delays the request by a configurable duration before proceeding.
3. **On response** — Captures status code, headers, body, and timing. Updates the log entry with full response data.

Header masking is applied at log time — the actual request/response objects are never mutated.

### 🔧 Usage

#### Basic

```kotlin
val client = HttpClient {
    install(WiretapKtorHttpPlugin)
}
```

All requests made with this client are automatically logged.

#### With Configuration

```kotlin
val client = HttpClient {
    install(WiretapKtorHttpPlugin) {
        enabled = true

        // Only log API requests
        shouldLog = { url, method -> url.contains("/api/") }

        // Mask sensitive headers
        headerAction = { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }

        // Auto-prune logs older than 7 days
        logRetention = LogRetention.Days(7)

        // Truncate bodies larger than 100 KB
        maxContentLength = 100 * 1024
    }
}
```

### 🛠️ Configuration

| Property            | Type                         | Default              | Description                                                                                      |
|---------------------|------------------------------|----------------------|--------------------------------------------------------------------------------------------------|
| `enabled`           | `Boolean`                    | `true`               | Master switch. When `false`, requests pass through with zero overhead.                            |
| `shouldLog`         | `(url, method) -> Boolean`   | `{ _, _ -> true }`   | Filter which requests are logged. Requests that don't match still respect mock/throttle rules.    |
| `headerAction`      | `(key) -> HeaderAction`      | `{ HeaderAction.Keep }` | Per-header control: `Keep`, `Skip`, or `Mask(mask)`.                                          |
| `logRetention`      | `LogRetention`               | `Forever`            | `Forever`, `AppSession` (clear on init), or `Days(n)` (auto-prune).                             |
| `maxContentLength`  | `Int`                        | `512000` (500 KB)    | Max characters for request/response bodies. Truncated before DB write. `0` disables body logging. |

### 🎭 Mock & Throttle Rules

Rules are managed through the Wiretap UI and stored in the Room database. The plugin evaluates rules on every request:

- **Mock** — Returns a fake response (configurable status code, body, headers) without making a network call. Optionally adds a delay before returning.
- **Throttle** — Delays the request by a fixed or randomized duration, then proceeds to the real network.

Rules match on method, URL (exact/contains/regex), headers, and body. First matching enabled rule wins.

---

## 🔌 WebSocket Plugin (`WiretapKtorWebSocketPlugin`)

### ⚙️ How It Works

`WiretapKtorWebSocketPlugin` intercepts WebSocket upgrade requests (status 101) and logs all sent/received frames. It works independently of `WiretapKtorHttpPlugin` — you can install either or both depending on your needs.

1. **On upgrade** — The plugin detects the 101 response and creates a `SocketConnection` entry with URL, request headers, status, and protocol.
2. **Session wrapping** — Call `wiretapped()` inside the `webSocket {}` block to get a `WiretapWebSocketSession` that logs all frames.
3. **Auto-close detection** — The session monitors its coroutine Job and automatically updates status to Closed/Failed when the connection ends.

### 📦 Setup

```kotlin
val client = HttpClient {
    install(WebSockets)
    install(WiretapKtorWebSocketPlugin) // WebSocket logging, needed to detekt 101 upgrades
}
```

### 🔧 Usage

```kotlin
client.webSocket("wss://example.com/ws") {
    val session = this.wiretapped() // IMPORTANT: Returns a DelegatingWebSocketSession if plugin is not installed

    // Send — automatically logged
    session?.send(Frame.Text("Hello!"))

    // Receive — automatically logged as frames are consumed
    for (frame in (session?.incoming ?: incoming)) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            // handle message
        }
    }
}
```

### What Gets Logged

| Event                      | Logged as                              |
|----------------------------|----------------------------------------|
| `session.send(Text)`       | Sent, full text content                |
| `session.send(Binary)`     | Sent, `[Binary: N bytes]`             |
| Receive Text from `incoming` | Received, full text content          |
| Receive Binary from `incoming` | Received, `[Binary: N bytes]`      |
| Ping/Pong frames           | Centered label with timestamp          |
| Close frame                | Centered label with code/reason        |
| Connection close           | Status updated to `Closed` with code/reason |
| Connection failure         | Status updated to `Failed` with error  |

### Closing a Connection

```kotlin
// Close with default code (1000) and optional reason
session.close(reason = "User disconnected")
```

Auto-close detection handles unexpected disconnections — no manual status management needed.
