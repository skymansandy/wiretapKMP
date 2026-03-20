# OkHttp Integration Guide

WiretapKMP provides an OkHttp `Interceptor` for HTTP logging and a `WebSocketListener` wrapper for WebSocket logging. Supports **Android** and **JVM Desktop**.

## Installation

Add `WiretapOkHttpInterceptor` to your OkHttp client:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        logRetention = LogRetention.Days(7)
        headerAction = { key ->
            if (key.equals("Authorization", ignoreCase = true))
                HeaderAction.Mask()
            else
                HeaderAction.Keep
        }
    })
    .build()
```

All configuration is optional — with no arguments, Wiretap logs every request.

## Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | `Boolean` | `true` | Master switch |
| `shouldLog` | `(url, method) -> Boolean` | `{ _, _ -> true }` | Filter which requests to capture |
| `headerAction` | `(key) -> HeaderAction` | `{ Keep }` | Control how headers are logged |
| `logRetention` | `LogRetention` | `Forever` | How long to keep log entries |

## How It Works

`WiretapOkHttpInterceptor` implements `Interceptor.intercept()`:

1. **Extract request** — URL, method, headers, body (serialized via okio)
2. **Evaluate rules** — Find matching mock/throttle rules
3. **Log request** — Entry appears immediately in the inspector
4. **Mock check** — If a mock rule matches, return a fake `Response` without network access
5. **Throttle check** — If a throttle rule matches, `Thread.sleep()` before proceeding
6. **Execute request** — `chain.proceed(request)` for real network call
7. **Capture response** — Status, headers, body, duration, TLS details
8. **Update log** — Complete the entry with response data

## TLS/Certificate Details

OkHttp's interceptor captures rich TLS information:

- **Protocol** — HTTP/1.1, HTTP/2, etc.
- **Remote address** — Host and port
- **TLS version** — TLSv1.2, TLSv1.3
- **Cipher suite** — e.g., TLS_AES_128_GCM_SHA256
- **Certificate CN** — Subject common name
- **Issuer CN** — Certificate authority
- **Certificate expiry** — Expiration date

These details appear in the inspector's **Overview** tab for each request.

## WebSocket Support

Wrap your `WebSocketListener` with `WiretapOkHttpWebSocketListener`:

```kotlin
val myListener = object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Note: webSocket is a WiretapWebSocket that logs outgoing messages
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // Handle incoming message
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // Handle close
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Handle failure
    }
}

val listener = WiretapOkHttpWebSocketListener(myListener)
val request = Request.Builder().url("wss://echo.websocket.org").build()
client.newWebSocket(request, listener)
```

The wrapper:

- Logs connection open/close/failure events
- Logs all incoming messages (text and binary)
- Wraps the `WebSocket` passed to `onOpen()` with a `WiretapWebSocket` that logs outgoing `send()` calls
- Delegates all events to your original listener

## Error Handling

- **Network errors** — Logged with response code `0`, error message as response body
- **Cancelled requests** — Logged with response code `-1`
- Exceptions are always re-thrown after logging

## Debug vs Release

```kotlin
// build.gradle.kts
debugImplementation("dev.skymansandy:wiretap-core:$version")
debugImplementation("dev.skymansandy:wiretap-okhttp:$version")
releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:$version")
```

The no-op interceptor calls `chain.proceed(chain.request())` directly. The no-op WebSocket listener delegates all events without logging.

## Complete Example

```kotlin
// Create client
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key ->
            when {
                key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
                key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
                else -> HeaderAction.Keep
            }
        }
        logRetention = LogRetention.AppSession
    })
    .build()

// HTTP request
val request = Request.Builder()
    .url("https://api.example.com/users")
    .header("Authorization", "Bearer token123")
    .build()

val response = client.newCall(request).execute()
// Authorization header logged as "***" in the inspector

// WebSocket
val wsRequest = Request.Builder().url("wss://ws.example.com").build()
val wsListener = WiretapOkHttpWebSocketListener(myWebSocketListener)
client.newWebSocket(wsRequest, wsListener)
```
