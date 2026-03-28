# wiretap-okhttp

OkHttp interceptor for WiretapKMP. Logs HTTP requests, responses, and WebSocket connections to the Wiretap database for inspection via the built-in Compose UI.

**Platforms:** Android, JVM

## 📦 Setup

```kotlin
// build.gradle.kts
dependencies {
    // Debug builds — full inspection (wiretap-core is included transitively)
    debugImplementation("dev.skymansandy:wiretap-okhttp:<version>")

    // Release builds — zero-overhead no-op stubs
    releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:<version>")
}
```

The noop module has an identical API surface — `WiretapOkHttpInterceptor` simply calls `chain.proceed(request)` and `WiretapOkHttpWebSocketListener` delegates all callbacks directly.

- [HTTP Interceptor (`WiretapOkHttpInterceptor`)](#-http-interceptor-wiretapokhttpinterceptor)
- [WebSocket Listener (`WiretapOkHttpWebSocketListener`)](#-websocket-listener-wiretapokhttpwebsocketlistener)

---

## 📡 HTTP Interceptor (`WiretapOkHttpInterceptor`)

### ⚙️ How It Works

`WiretapOkHttpInterceptor` implements OkHttp's `Interceptor` interface. When added to an `OkHttpClient`, it intercepts every request:

1. **Request capture** — Extracts URL, method, headers, and body. Checks for matching mock/throttle rules. Logs the request to the Room database.
2. **Rule evaluation** — If a **mock rule** matches, builds a fake `Response` and returns it immediately without calling `chain.proceed()`. If a **throttle rule** matches, sleeps for the configured duration before proceeding to the network.
3. **Response capture** — Captures status code, headers, body (via `peekBody`), and timing. Also extracts TLS details: protocol version, cipher suite, peer certificate subject/issuer CN, and expiration.
4. **Error handling** — Network errors are logged with status `0`, cancelled requests with `-1`. Exceptions are always re-thrown to preserve OkHttp's error semantics.

Header masking is applied at log time — the actual request/response objects are never mutated.

### 🔧 Usage

#### Basic

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor())
    .build()
```

All requests made with this client are automatically logged.

#### With Configuration

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        enabled = true

        // Only log API requests
        shouldLog = { url, method -> url.contains("/api/") }

        // Mask sensitive headers
        headerAction = { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("X-Api-Key", ignoreCase = true) -> HeaderAction.Mask("REDACTED")
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }

        // Auto-prune logs older than 7 days
        logRetention = LogRetention.Days(7)
    })
    .build()
```

### 🛠️ Configuration

| Property       | Type                         | Default              | Description                                                                                      |
|----------------|------------------------------|----------------------|--------------------------------------------------------------------------------------------------|
| `enabled`      | `Boolean`                    | `true`               | Master switch. When `false`, requests pass through with zero overhead.                            |
| `shouldLog`    | `(url, method) -> Boolean`   | `{ _, _ -> true }`   | Filter which requests are logged. Requests that don't match still respect mock/throttle rules.    |
| `headerAction` | `(key) -> HeaderAction`      | `{ HeaderAction.Keep }` | Per-header control: `Keep`, `Skip`, or `Mask(mask)`.                                          |
| `logRetention` | `LogRetention`               | `Forever`            | `Forever`, `AppSession` (clear on init), or `Days(n)` (auto-prune).                             |

### 🎭 Mock & Throttle Rules

Rules are managed through the Wiretap UI and stored in the Room database. The interceptor evaluates rules on every request:

- **Mock** — Returns a fake `Response` (configurable status code, body, headers) without making a network call. Optionally adds a delay before returning.
- **Throttle** — Blocks the thread for a fixed or randomized duration, then proceeds to the real network.

Rules match on method, URL (exact/contains/regex), headers, and body. First matching enabled rule wins.

### 🔒 TLS Details

Unlike other interceptors, the OkHttp interceptor captures rich TLS information from `response.handshake`:

- Protocol version (e.g., TLS 1.3)
- Cipher suite
- Peer certificate subject CN, issuer CN, and expiration date
- Remote socket address

---

## 🔌 WebSocket Listener (`WiretapOkHttpWebSocketListener`)

### ⚙️ How It Works

`WiretapOkHttpWebSocketListener` wraps your `WebSocketListener` to log all connection lifecycle events and messages. Outgoing messages are automatically intercepted via an internal `WiretapWebSocket` wrapper — no extra code needed.

1. **On open** — Creates a `SocketConnection` entry with URL, request headers, protocol, and status `Open`. The `WebSocket` passed to your `onOpen` callback is transparently wrapped so that `send()` calls are logged.
2. **On message** — Incoming text and binary messages are logged automatically before being forwarded to your delegate.
3. **On close/failure** — Connection status is updated to `Closing`, `Closed`, or `Failed` with the close code, reason, or error message.

### 🔧 Usage

#### Extension function (recommended)

```kotlin
val request = Request.Builder().url("wss://example.com/ws").build()

client.newWebSocket(request, myListener.wiretapped())
```

#### Constructor

```kotlin
val request = Request.Builder().url("wss://example.com/ws").build()

client.newWebSocket(
    request,
    WiretapOkHttpWebSocketListener(
        object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // webSocket is automatically wrapped — send() calls are logged
                webSocket.send("Hello!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Incoming messages are logged before reaching here
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // Connection closed — status logged
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // Connection failed — error logged
            }
        },
    ),
)
```

### What Gets Logged

| Event                          | Logged as                              |
|--------------------------------|----------------------------------------|
| `onOpen()`                     | `SocketConnection` with status `Open`  |
| `webSocket.send(text)`         | Sent, full text content                |
| `webSocket.send(bytes)`        | Sent, `[Binary: N bytes]`             |
| `onMessage(webSocket, text)`   | Received, full text content            |
| `onMessage(webSocket, bytes)`  | Received, `[Binary: N bytes]`         |
| `onClosing(code, reason)`      | Status updated to `Closing`            |
| `onClosed(code, reason)`       | Status updated to `Closed` with code/reason |
| `onFailure(throwable)`         | Status updated to `Failed` with error  |

The noop module (`wiretap-okhttp-noop`) provides the same `WiretapOkHttpWebSocketListener` class and `wiretapped()` extension but delegates all callbacks directly without logging.
