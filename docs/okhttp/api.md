# OkHttp — API Reference

## WiretapOkHttpInterceptor

```kotlin
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor, KoinComponent
```

OkHttp `Interceptor` for HTTP request/response logging with mock/throttle rule support.

### Constructor

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `configure` | `WiretapConfig.() -> Unit` | `{}` | Configuration builder lambda |

### Installation

```kotlin
OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        // WiretapConfig properties — all optional
    })
    .build()
```

### TLS Details Captured

| Field | Source |
|-------|--------|
| `protocol` | `response.protocol` |
| `remoteAddress` | `chain.connection()?.route()?.socketAddress` |
| `tlsProtocol` | `handshake.tlsVersion.javaName` |
| `cipherSuite` | `handshake.cipherSuite.javaName` |
| `certificateCn` | Peer certificate subject CN |
| `issuerCn` | Peer certificate issuer CN |
| `certificateExpiry` | `peerCert.notAfter` |

---

## WiretapOkHttpWebSocketListener

```kotlin
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener(), KoinComponent
```

Wraps a consumer's `WebSocketListener` to log all WebSocket events.

### Installation

```kotlin
// Extension function (recommended)
client.newWebSocket(request, myListener.wiretapped())

// Constructor
client.newWebSocket(request, WiretapOkHttpWebSocketListener(myListener))
```

### Intercepted Callbacks

| Callback | Logged As |
|----------|-----------|
| `onOpen(webSocket, response)` | Connection opened. WebSocket wrapped for outgoing logging |
| `onMessage(webSocket, text)` | Text message received |
| `onMessage(webSocket, bytes)` | Binary message received |
| `onClosing(webSocket, code, reason)` | Status → Closing |
| `onClosed(webSocket, code, reason)` | Status → Closed |
| `onFailure(webSocket, t, response)` | Status → Failed |

---

## wiretapped()

```kotlin
fun WebSocketListener.wiretapped(): WiretapOkHttpWebSocketListener
```

Extension function that wraps the receiver in a `WiretapOkHttpWebSocketListener`. Available in both the debug and noop modules.

---

## WiretapConfig

```kotlin
class WiretapConfig {
    var enabled: Boolean = true
    var shouldLog: (url: String, method: String) -> Boolean = { _, _ -> true }
    var headerAction: (key: String) -> HeaderAction = { HeaderAction.Keep }
    var logRetention: LogRetention = LogRetention.Forever
    var maxContentLength: Int = MAX_CONTENT_LENGTH  // 500 * 1024
}
```

---

## No-op (wiretap-okhttp-noop)

| Component | Behavior |
|-----------|----------|
| `WiretapOkHttpInterceptor` | `chain.proceed(chain.request())` |
| `WiretapOkHttpWebSocketListener` | Pure delegation to original listener |
| `wiretapModule` | Empty Koin module |

Same constructor signatures — zero overhead, drop-in replacement.
