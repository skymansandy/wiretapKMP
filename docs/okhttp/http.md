# OkHttp — HTTP Logging

=== "Overview"

    ![HTTP Overview](../assets/screenshots/http/overview.png){ width="300" }

=== "Request"

    ![HTTP Request](../assets/screenshots/http/request.png){ width="300" }

=== "Response"

    ![HTTP Response](../assets/screenshots/http/respose.png){ width="300" }

## How It Works

`WiretapOkHttpInterceptor` implements `Interceptor.intercept(chain)`:

1. **Early exit** — Skips if `enabled = false` or if request is a WebSocket upgrade
2. **Extract request** — URL, method, headers, body (serialized via okio `Buffer`)
3. **Evaluate rules** — `FindMatchingRuleUseCase` checks for matching mock/throttle rules
4. **Log request** — Entry appears immediately in the inspector (gated by `shouldLog`)
5. **Mock check** — If a mock rule matches, builds a fake `Response` and returns immediately
6. **Throttle check** — If a throttle rule matches, `delay()` before proceeding
7. **Execute request** — `chain.proceed(request)` for real network call
8. **Capture response** — Status, headers, body (via `peekBody()`), duration, TLS details
9. **Update log** — Complete the entry with response data

## TLS/Certificate Details

OkHttp's interceptor captures rich TLS information not available in Ktor:

| Field | Description |
|-------|-------------|
| `protocol` | HTTP/1.1, HTTP/2, etc. |
| `remoteAddress` | Host and port of the server |
| `tlsProtocol` | TLSv1.2, TLSv1.3 |
| `cipherSuite` | e.g., TLS_AES_128_GCM_SHA256 |
| `certificateCn` | Subject common name |
| `issuerCn` | Certificate authority CN |
| `certificateExpiry` | Certificate expiration date |

These details appear in the inspector's **Overview** tab for each request.

## Request Filtering

```kotlin
WiretapOkHttpInterceptor {
    shouldLog = { url, method ->
        url.contains("/api/") && method != "OPTIONS"
    }
}
```

## Header Masking

```kotlin
WiretapOkHttpInterceptor {
    headerAction = { key ->
        when {
            key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
            key.equals("X-Api-Key", ignoreCase = true) -> HeaderAction.Mask("REDACTED")
            key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
            else -> HeaderAction.Keep
        }
    }
}
```

## Mock Rules

When a request matches a mock rule, the interceptor builds a fake OkHttp `Response` using `Protocol.HTTP_1_1` and `"Mock"` as the message. Mock responses appear in the inspector with a **Mock** badge.

=== "Mocked Requests"

    ![Mocked Requests](../assets/screenshots/http/mocked requests.png){ width="300" }

=== "Mocked Response"

    ![Mocked Response](../assets/screenshots/http/mocked_response.png){ width="300" }

=== "Mock Rule Setup"

    ![Mock Rule](../assets/screenshots/http/just mock.png){ width="300" }

## Throttle Rules

Throttle rules use coroutine `delay()` (non-blocking) before `chain.proceed()`. The real network call still happens ��� throttling only adds delay.

=== "Throttle Rule Setup"

    ![Throttle Rule](../assets/screenshots/http/throttle only.png){ width="300" }

=== "Mock + Throttle"

    ![Mock + Throttle Rule](../assets/screenshots/http/mock+throttle.png){ width="300" }

## Error Handling

| Scenario | Response Code | Response Body |
|----------|:------------:|---------------|
| Network error | `0` | Exception message |
| Cancelled request (`"Canceled"`) | `-1` | Exception message |
| In-progress | `-2` | — |

Exceptions are always re-thrown after logging.

## Complete Example

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor {
        shouldLog = { url, _ -> url.contains("/api/") }
        headerAction = { key ->
            if (key.equals("Authorization", ignoreCase = true))
                HeaderAction.Mask()
            else
                HeaderAction.Keep
        }
        logRetention = LogRetention.AppSession
    })
    .build()

// GET request
val request = Request.Builder()
    .url("https://api.example.com/users")
    .header("Authorization", "Bearer token123")
    .build()

val response = client.newCall(request).execute()
// Authorization header logged as "***" in the inspector

// POST request
val postBody = """{"name": "Alice"}"""
    .toRequestBody("application/json".toMediaType())

val postRequest = Request.Builder()
    .url("https://api.example.com/users")
    .post(postBody)
    .build()

client.newCall(postRequest).execute()
```
