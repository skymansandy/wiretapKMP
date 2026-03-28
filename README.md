<h1 align="center">WiretapKMP</h1>

<p align="center">
  <img src="art/wiretap_icon.png" width="120" alt="WiretapKMP Icon"/>
</p>

<p align="center">
  <a href="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml"><img src="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml/badge.svg" alt="Build"/></a>
  <a href="https://github.com/skymansandy/wiretapKMP/actions/workflows/deploy.yml"><img src="https://img.shields.io/badge/coverage-100%25-brightgreen" alt="Coverage"/></a>
  <a href="https://central.sonatype.com/search?q=dev.skymansandy+wiretap"><img src="https://img.shields.io/badge/maven--central-1.0.0--RC3-blue" alt="Maven Central"/></a>
</p>

Kotlin Multiplatform network inspection and mocking SDK. Intercept HTTP and WebSocket traffic, mock API responses, and throttle requests — no proxy server needed.

## 📱 Platforms

### KMP Plugins

| Client | Android | iOS | JVM Desktop |
|--------|:-------:|:---:|:-----------:|
| **Ktor** | ✅ | ✅ | ✅ |
| **OkHttp** | ✅ | — | ✅ |

### Swift UrlSession

| Client                | iOS |
|-----------------------|:---:|
| **WiretapURLSession** | ✅ |

`wiretap-urlsession` is a dedicated Swift wrapper exported as an XCFramework via KMMBridge/SPM.

## ✨ Features

- **API Mocking** — Return fake responses without hitting the network. Match on method, URL, headers, and body.
- **Request Throttling** — Add artificial delay with fixed or random ranges.
- **HTTP Logging** — Capture URL, method, headers, bodies, status codes, duration, TLS details (OkHttp).
- **WebSocket Logging** — Full lifecycle tracking with message capture for Ktor and OkHttp.
- **Header Masking** — Keep, mask, or skip headers from logs.
- **Log Retention** — Forever, per app session, or time-based auto-pruning.
- **Built-in Inspector UI** — Compose Multiplatform UI for browsing logs, WebSocket streams, and managing rules.
- **No-op Variants** — Drop-in release replacements with zero overhead.

## 📖 Plugin Documentation

Each plugin module has its own README with detailed setup, configuration, usage examples, WebSocket support, and mock/throttle rules:

| Plugin                | Platforms         | README                                                          |
|-----------------------|-------------------|-----------------------------------------------------------------|
| **Ktor**              | Android, iOS, JVM | [`wiretap-ktor/README.md`](wiretap-ktor/README.md)             |
| **OkHttp**            | Android, JVM      | [`wiretap-okhttp/README.md`](wiretap-okhttp/README.md)         |
| **WiretapURLSession** | iOS               | [`wiretap-urlsession/README.md`](wiretap-urlsession/README.md) |

## 🔇 No-op Variants

Swap dependencies for release builds — no conditional code needed.

| Debug            | Release                |
|------------------|------------------------|
| `wiretap-ktor`   | `wiretap-ktor-noop`   |
| `wiretap-okhttp` | `wiretap-okhttp-noop` |

For URLSession, use `WiretapURLSession` in debug and plain `URLSession` in release (see installation above).

## 📚 Documentation

[Full documentation](https://skymansandy.dev/wiretapKMP/)

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/my-feature`)
3. **Commit** your changes (`git commit -m 'Add my feature'`)
4. **Push** to the branch (`git push origin feature/my-feature`)
5. **Open** a Pull Request

## 🙏 Acknowledgements

- [JetBrains](https://www.jetbrains.com/) — for [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html), [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/), and [Ktor](https://ktor.io/)
- [Android Jetpack](https://developer.android.com/jetpack) — for [Room](https://developer.android.com/kotlin/multiplatform/room), [App Startup](https://developer.android.com/topic/libraries/app-startup), and [Compose](https://developer.android.com/develop/ui/compose)
- [Koin](https://insert-koin.io/) — lightweight dependency injection for KMP
- [OkHttp](https://square.github.io/okhttp/) — by Square, for the HTTP client and interceptor APIs
- [SKIE](https://skie.touchlab.co/) — by Touchlab, for Swift-friendly KMP interop
- [KMMBridge](https://kmmbridge.touchlab.co/) — by Touchlab, for SPM publishing of KMP frameworks

## 📄 License

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
