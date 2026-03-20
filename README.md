# WiretapKMP

Kotlin Multiplatform network inspection and mocking SDK. Intercept HTTP and WebSocket traffic, mock API responses, and throttle requests — no proxy server needed.

## Platforms

### KMP Plugins

| Client | Android | iOS | JVM Desktop |
|--------|:-------:|:---:|:-----------:|
| **Ktor** | ✅ | ✅ | ✅ |
| **OkHttp** | ✅ | — | ✅ |

### Swift Wrapper

| Client | iOS |
|--------|:---:|
| **URLSession** | ✅ |

`wiretap-urlsession` is a dedicated Swift wrapper exported as an XCFramework via KMMBridge/SPM.

## Features

- **API Mocking** — Return fake responses without hitting the network. Match on method, URL, headers, and body.
- **Request Throttling** — Add artificial delay with fixed or random ranges.
- **HTTP Logging** — Capture URL, method, headers, bodies, status codes, duration, TLS details (OkHttp).
- **WebSocket Logging** — Full lifecycle tracking with message capture for Ktor and OkHttp.
- **Header Masking** — Keep, mask, or skip headers from logs.
- **Log Retention** — Forever, per app session, or time-based auto-pruning.
- **Built-in Inspector UI** — Compose Multiplatform UI for browsing logs, WebSocket streams, and managing rules.
- **No-op Variants** — Drop-in release replacements with zero overhead.

## Usage

### Ktor plugin

```kotlin
val client = HttpClient {
    install(WiretapKtorPlugin) {
        enabled = true
        logRetention = LogRetention.Days(7)
    }
    install(WiretapKtorWebSocketPlugin)
}
```

### OkHttp interceptor

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        WiretapOkHttpInterceptor {
            enabled = true
            logRetention = LogRetention.Days(7)
        }
    )
    .build()
```

### URLSession (Swift)

```swift
let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = true
    config.logRetention = LogRetention.Days(days: 7)
}

interceptor.intercept(request: request) { data, response, error in
    // handle response
}
```

## No-op Variants

Swap dependencies for release builds — no conditional code needed.

| Debug | Release |
|-------|---------|
| `wiretap-ktor` | `wiretap-ktor-noop` |
| `wiretap-okhttp` | `wiretap-okhttp-noop` |
| `wiretap-urlsession` | `wiretap-urlsession-noop` |

## Documentation

[Full documentation](https://skymansandy.github.io/wiretapKMP/)

## License

```
Copyright 2025 skymansandy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
