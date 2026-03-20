# URLSession Integration Guide (iOS/Swift)

WiretapKMP provides a `WiretapURLSessionInterceptor` for native iOS apps using `NSURLSession`. Published as a static framework via Swift Package Manager.

## Installation

### Swift Package Manager

Add the WiretapKMP SPM package dependency, then link the `WiretapURLSession` framework.

### XcodeGen Example

```yaml
packages:
  WiretapURLSession:
    path: path/to/WiretapKMP

targets:
  MyApp:
    dependencies:
      - package: WiretapURLSession
    preBuildScripts:
      - name: "Compile Kotlin Frameworks"
        script: |
          cd "$SRCROOT/path/to/WiretapKMP"
          ./gradlew :wiretap-urlsession:spmDevBuild \
            -PspmBuildTargets=ios_simulator_arm64
```

## Setup

### Create the Interceptor

```swift
import WiretapURLSession

let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = true
    config.logRetention = LogRetention.Days(days: 7)
    config.shouldLog = { url, method in
        KotlinBoolean(value: url.contains("/api/"))
    }
    config.headerAction = { key in
        if key.caseInsensitiveCompare("Authorization") == .orderedSame {
            return HeaderAction.Mask(mask: "***")
        }
        return HeaderAction.Keep.shared
    }
}
```

### Enable the Inspector UI

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

## Two APIs

WiretapURLSessionInterceptor provides two ways to make requests:

### `intercept()` — Full Rule Support

Fire-and-forget execution with **mock and throttle rule support**:

```swift
let url = URL(string: "https://api.example.com/users")!
let request = URLRequest(url: url)

interceptor.intercept(request: request) { data, response, error in
    guard let data = data, error == nil else {
        print("Error: \(error?.localizedDescription ?? "Unknown")")
        return
    }
    let json = try? JSONSerialization.jsonObject(with: data)
    print(json ?? "No data")
}
```

- Automatically executes the request and calls the completion handler
- If a **mock rule** matches, returns mock data without network access
- If a **throttle rule** matches, delays execution using GCD (`dispatch_after`)
- No task object returned — you cannot cancel the request

### `dataTask()` — Logging Only

Returns an `NSURLSessionDataTask` for standard URLSession patterns:

```swift
let task = interceptor.dataTask(request: request) { data, response, error in
    // handle response
}
task.resume()

// Cancel support
DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
    task.cancel()
}
```

- Returns the data task — **caller must call `resume()`**
- **Logging only** — mock and throttle rules are NOT applied
- Supports cancellation via `task.cancel()`

### URL String Convenience

```swift
let task = interceptor.dataTask(url: "https://api.example.com/users") { data, response, error in
    // handle response
}
task.resume()
```

## When to Use Which API

| Feature | `intercept()` | `dataTask()` |
|---------|:------------:|:------------:|
| HTTP logging | Yes | Yes |
| Mock rules | Yes | No |
| Throttle rules | Yes | No |
| Cancel support | No | Yes |
| Returns task | No | Yes |

**Use `intercept()`** when you need rule support (testing, development).
**Use `dataTask()`** when you need task control (cancellation, progress).

## Configuration Options

| Property | Swift Type | Default | Description |
|----------|-----------|---------|-------------|
| `enabled` | `Bool` | `true` | Master switch |
| `shouldLog` | `(String, String) -> KotlinBoolean` | logs all | Filter requests |
| `headerAction` | `(String) -> HeaderAction` | `Keep` | Mask/skip headers |
| `logRetention` | `LogRetention` | `Forever` | Retention policy |

!!! note "Swift Type Bridging"
    Due to Kotlin/Native interop, `shouldLog` returns `KotlinBoolean` instead of Swift `Bool`. Use `KotlinBoolean(value: true/false)`.

## POST Request Example

```swift
var request = URLRequest(url: URL(string: "https://api.example.com/users")!)
request.httpMethod = "POST"
request.setValue("application/json", forHTTPHeaderField: "Content-Type")
request.httpBody = try? JSONSerialization.data(withJSONObject: [
    "name": "John Doe",
    "email": "john@example.com",
])

interceptor.intercept(request: request) { data, response, error in
    // Request body is captured in the log
}
```

## Debug vs Release

For release builds, swap to the no-op framework:

```swift
// Debug: WiretapURLSession (full implementation)
// Release: WiretapURLSession from wiretap-urlsession-noop (pass-through)
```

The no-op interceptor:

- Accepts the same constructor parameters (config is ignored)
- `intercept()` executes the request directly via `session.dataTaskWithRequest().resume()`
- `dataTask()` returns `session.dataTaskWithRequest()` directly
- No Koin, no database, no overhead
