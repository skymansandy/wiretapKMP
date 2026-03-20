# OkHttp Interceptor API Reference

## WiretapOkHttpInterceptor

OkHttp `Interceptor` for HTTP request/response logging with rule support.

```kotlin
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor, KoinComponent
```

### Constructor

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `configure` | `WiretapConfig.() -> Unit` | `{}` | Configuration builder lambda |

### Installation

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        enabled = true
        shouldLog = { url, method -> true }
        headerAction = { key -> HeaderAction.Keep }
        logRetention = LogRetention.Forever
    })
    .build()
```

### Behavior

Implements `Interceptor.intercept(chain)`:

1. Skips if `enabled = false`
2. Skips WebSocket upgrade requests (header `Upgrade: websocket`)
3. Evaluates mock/throttle rules
4. Logs request → executes/mocks → logs response
5. Captures TLS details (protocol, cipher, certificate CN, issuer, expiry)

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

Wraps a `WebSocketListener` to log all WebSocket events.

```kotlin
class WiretapOkHttpWebSocketListener(
    private val delegate: WebSocketListener,
) : WebSocketListener(), KoinComponent
```

### Constructor

| Parameter | Type | Description |
|-----------|------|-------------|
| `delegate` | `WebSocketListener` | Your original listener |

### Intercepted Callbacks

| Callback | Logged As |
|----------|-----------|
| `onOpen(webSocket, response)` | Connection opened (status: Open). WebSocket wrapped for outgoing logging |
| `onMessage(webSocket, text)` | Text message received |
| `onMessage(webSocket, bytes)` | Binary message received (`[Binary: N bytes]`) |
| `onClosing(webSocket, code, reason)` | Status → Closing |
| `onClosed(webSocket, code, reason)` | Status → Closed |
| `onFailure(webSocket, t, response)` | Status → Failed |

All callbacks delegate to the original listener after logging.

### Usage

```kotlin
val listener = WiretapOkHttpWebSocketListener(myWebSocketListener)
client.newWebSocket(request, listener)
```

---

## WiretapWebSocket (Internal)

Internal `WebSocket` wrapper that logs outgoing `send()` calls.

```kotlin
internal class WiretapWebSocket(
    private val delegate: WebSocket,
    private val socketId: Long,
    private val orchestrator: WiretapOrchestrator,
) : WebSocket by delegate
```

Overrides `send(text)` and `send(bytes)` to log messages with direction `Sent`. All other methods delegate to the original WebSocket.

Passed to your listener's `onOpen()` callback transparently.

---

## No-op Module (wiretap-okhttp-noop)

### WiretapOkHttpInterceptor

```kotlin
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor {
    override fun intercept(chain: Chain): Response =
        chain.proceed(chain.request())
}
```

### WiretapOkHttpWebSocketListener

Pure delegation — all callbacks forwarded to delegate without logging.

### Wiretap Object

```kotlin
object Wiretap {
    val koinModule: Module  // empty
}
```
