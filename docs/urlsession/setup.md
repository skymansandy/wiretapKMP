# URLSession — Setup

**Platforms:** iOS (iosArm64 + iosSimulatorArm64)

## Dependencies

### Swift Package Manager

Add the WiretapKMP SPM package via its GitHub repository URL, then link the `WiretapURLSession` framework.

=== "Xcode"

    1. **File → Add Package Dependencies…**
    2. Enter the repository URL:
       ```
       https://github.com/skymansandy/wiretapKMP.git
       ```
    3. Set the version rule (e.g. **Up to Next Major** from `1.0.0-RC2`)
    4. Add the `WiretapURLSession` library to your target

=== "Package.swift"

    ```swift
    dependencies: [
        .package(
            url: "https://github.com/skymansandy/wiretapKMP.git",
            from: "1.0.0-RC2"
        )
    ]
    ```
    Then add the dependency to your target:
    ```swift
    .target(
        name: "MyApp",
        dependencies: [
            .product(name: "WiretapURLSession", package: "wiretapKMP")
        ]
    )
    ```

=== "XcodeGen"

    ```yaml
    packages:
      WiretapURLSession:
        url: https://github.com/skymansandy/wiretapKMP.git
        from: 1.0.0-RC2

    targets:
      MyApp:
        dependencies:
          - package: WiretapURLSession
    ```

## Create a Session

Use `WiretapSession` in debug builds and plain `URLSession` in release — zero framework overhead in production:

```swift
#if DEBUG
import WiretapURLSession
#endif

class NetworkClient {

    #if DEBUG
    private let session: WiretapSession

    init() {
        session = WiretapSession(configuration: .default) { config in
            config.enabled = true
            config.shouldLog = { url, method in
                KotlinBoolean(value: url.contains("/api/"))
            }
            config.headerAction = { key in
                if key.caseInsensitiveCompare("Authorization") == .orderedSame {
                    return HeaderActionMask(mask: "***")
                }
                if key.caseInsensitiveCompare("Cookie") == .orderedSame {
                    return HeaderActionSkip.shared
                }
                return HeaderActionKeep.shared
            }
            config.logRetention = LogRetentionDays(days: 7)
        }
    }
    #else
    private let session = URLSession.shared

    init() {}
    #endif
}
```

Then bridge the two session types with a pair of private helpers so the rest of your networking code stays `#if`-free:

```swift
private func execute(
    _ request: URLRequest,
    completion: @escaping (Data?, URLResponse?, Error?) -> Void
) {
    #if DEBUG
    session.intercept(request: request) { data, response, error in
        completion(data as Data?, response, error)
    }
    #else
    session.dataTask(with: request) { data, response, error in
        completion(data, response, error)
    }.resume()
    #endif
}

private func createTask(
    _ request: URLRequest,
    completion: @escaping (Data?, URLResponse?, Error?) -> Void
) -> URLSessionDataTask {
    #if DEBUG
    session.dataTask(request: request) { data, response, error in
        completion(data as Data?, response, error)
    }
    #else
    session.dataTask(with: request) { data, response, error in
        completion(data, response, error)
    }
    #endif
}
```

All other methods just call `execute()` or `createTask()` — no `#if DEBUG` needed.

??? note "Advanced: Using your own URLSession"
    If you need a custom `URLSession` instance (e.g. custom delegate or shared session),
    use `WiretapURLSessionInterceptor` directly:

    ```swift
    let interceptor = WiretapURLSessionInterceptor(session: mySession) { config in
        config.enabled = true
    }
    ```

## Enable the Inspector UI

```swift
@main
struct MyApp: App {
    init() {
        WiretapLauncher_iosKt.enableLaunchTool()
    }

    var body: some Scene {
        WindowGroup { ContentView() }
    }
}
```

## Configuration Reference

| Property | Swift Type | Default | Description |
|----------|-----------|---------|-------------|
| `enabled` | `Bool` | `true` | Master switch — `false` disables all logging |
| `shouldLog` | `(String, String) -> KotlinBoolean` | logs all | Filter which requests to capture |
| `headerAction` | `(String) -> HeaderAction` | `HeaderActionKeep` | Control how headers are logged |
| `logRetention` | `LogRetention` | `LogRetentionForever` | How long to keep log entries |

### `enabled`

```swift
config.enabled = false  // Disable entirely — requests pass through
```

### `shouldLog`

```swift
config.shouldLog = { url, method in
    KotlinBoolean(value: url.contains("/api/"))
}
```

!!! note "Swift Type Bridging"
    `shouldLog` returns `KotlinBoolean` instead of Swift `Bool` due to Kotlin/Native interop. Use `KotlinBoolean(value: true/false)`.

### `headerAction`

```swift
config.headerAction = { key in
    if key.caseInsensitiveCompare("Authorization") == .orderedSame {
        return HeaderActionMask(mask: "***")
    }
    if key.caseInsensitiveCompare("Cookie") == .orderedSame {
        return HeaderActionSkip.shared
    }
    return HeaderActionKeep.shared
}
```

!!! note "Swift Singletons"
    `HeaderActionKeep` and `HeaderActionSkip` are Kotlin objects — access them via `.shared` in Swift. `HeaderActionMask` is a data class and is constructed normally.

### `logRetention`

```swift
config.logRetention = LogRetentionForever.shared
config.logRetention = LogRetentionAppSession.shared
config.logRetention = LogRetentionDays(days: 7)
```
