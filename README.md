<h1 align="center">WiretapKMP</h1>

<p align="center">
  <img src="art/wiretap_icon.png" width="120" alt="WiretapKMP Icon"/>
</p>

<p align="center">
  <a href="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml"><img src="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml/badge.svg" alt="Build"/></a>
  <a href="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml"><img src="https://img.shields.io/badge/coverage-0%25-red" alt="Coverage"/></a>
  <a href="https://central.sonatype.com/search?q=dev.skymansandy+wiretap"><img src="https://img.shields.io/badge/maven--central-1.0.0--beta1-blue" alt="Maven Central"/></a>
</p>

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

## Installation

WiretapKMP is published to Maven Central.

### 1. Add Maven Central repository

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

### 2. Add dependencies

#### Ktor

```kotlin
dependencies {
    // Debug
    debugImplementation("dev.skymansandy:wiretap-ktor:1.0.0-beta1")
    // Release (no-op)
    releaseImplementation("dev.skymansandy:wiretap-ktor-noop:1.0.0-beta1")
}
```

#### OkHttp

```kotlin
dependencies {
    // Debug
    debugImplementation("dev.skymansandy:wiretap-okhttp:1.0.0-beta1")
    // Release (no-op)
    releaseImplementation("dev.skymansandy:wiretap-okhttp-noop:1.0.0-beta1")
}
```

#### URLSession (iOS via SPM)

```swift
// Debug: wiretap-core + wiretap-urlsession frameworks
// Release: wiretap-urlsession-noop framework
```

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
