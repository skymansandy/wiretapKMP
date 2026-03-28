# wiretap-urlsession

URLSession interceptor for WiretapKMP. Logs HTTP requests and responses from Apple's URLSession to the Wiretap database for inspection via the built-in Compose UI.

**Platforms:** iOS (iosArm64, iosSimulatorArm64)

**Framework:** `WiretapURLSession` (distributed via SPM using KMMBridge)

## ⚙️ How It Works

The module provides two ways to integrate with URLSession:

### WiretapURLSession (recommended)

A drop-in URLSession-like wrapper that manages the underlying `NSURLSession` and interceptor for you.

### WiretapURLSessionInterceptor

The lower-level interceptor class with two distinct APIs:

- **`intercept(request, completionHandler)`** — Fire-and-forget. Automatically executes the request, applies mock/throttle rules, and calls the completion handler. No need to call `resume()`.
- **`dataTask(request, completionHandler)`** — Returns an `NSURLSessionDataTask`. Caller must call `resume()`. Logs requests/responses but does **not** apply mock/throttle rules (use `intercept()` for that).

Both APIs capture URL, method, headers, body, response data, timing, and cancellation state. Header masking is applied at log time — the original `NSURLRequest` is never mutated.

## 📦 Setup

### SPM (Swift Package Manager)

Add the `WiretapURLSession` package to your Xcode project. The framework is published via KMMBridge.

For local development with XcodeGen, add a pre-build script:

```yaml
preBuildScripts:
  - name: "Compile Kotlin Frameworks"
    script: |
      cd "$SRCROOT/.."
      ./gradlew :wiretap-urlsession:spmDevBuild -PspmBuildTargets=ios_simulator_arm64
```

### Debug/Release Pattern

Use compile-time conditionals — no separate noop module needed:

```swift
#if DEBUG
import WiretapURLSession
#endif
```

## 🔧 Usage (Swift)

### Basic — WiretapURLSession

```swift
#if DEBUG
import WiretapURLSession

let session = WiretapURLSession(configuration: .default) { config in
    config.enabled = true
}
#else
let session = URLSession.shared
#endif
```

### With Configuration

```swift
let session = WiretapURLSession(configuration: .default) { config in
    config.enabled = true
    config.logRetention = LogRetentionDays(days: 7)
    config.headerAction = { key in
        if key.caseInsensitiveCompare("Authorization") == .orderedSame {
            return HeaderActionMask(mask: "***")
        }
        return HeaderActionKeep.shared
    }
    config.shouldLog = { _, _ in
        return KotlinBoolean(value: true)
    }
}
```

### Fire-and-Forget (with mock/throttle rules)

```swift
session.intercept(request: urlRequest) { data, response, error in
    // Called automatically — no need to resume()
    guard let data = data as Data?, let response = response else { return }
    // handle response
}
```

### Task-Based (with cancellation control)

```swift
let task = session.dataTask(request: urlRequest) { data, response, error in
    guard let data = data as Data?, let response = response else { return }
    // handle response
}
task.resume()

// Later...
task.cancel() // Cancellation is logged automatically
```

### Custom Session Configuration (e.g., timeout)

```swift
let config = URLSessionConfiguration.default
config.timeoutIntervalForRequest = 5.0

let timeoutSession = WiretapURLSession(configuration: config) { _ in }
timeoutSession.intercept(request: request) { data, response, error in
    // 5-second timeout applied
}
```

## 🛠️ Configuration

| Property       | Type                         | Default              | Description                                                                          |
|----------------|------------------------------|----------------------|--------------------------------------------------------------------------------------|
| `enabled`      | `Boolean`                    | `true`               | Master switch. When `false`, requests pass through to raw URLSession with zero overhead. |
| `shouldLog`    | `(url, method) -> Boolean`   | `{ _, _ -> true }`   | Filter which requests are logged.                                                     |
| `headerAction` | `(key) -> HeaderAction`      | `{ HeaderAction.Keep }` | Per-header control: `Keep`, `Skip`, or `Mask(mask)`.                               |
| `logRetention` | `LogRetention`               | `Forever`            | `Forever`, `AppSession` (clear on init), or `Days(n)` (auto-prune).                  |

## 🎭 Mock & Throttle Rules

Rules are managed through the Wiretap UI and stored in the Room database. **Only `intercept()` evaluates rules** — `dataTask()` does not.

- **Mock** — Returns a fake response (configurable status code, body, headers) without making a network call. Optionally adds a delay before returning.
- **Throttle** — Delays the request using `dispatch_after` (non-blocking), then proceeds to the real network. Delay can be fixed or randomized between a min/max range.

Rules match on method, URL (exact/contains/regex), headers, and body. First matching enabled rule wins.

## 📊 API Comparison

| Feature | `intercept()` | `dataTask()` |
|---|---|---|
| Executes request automatically | Yes | No (must call `resume()`) |
| Mock/throttle rules | Yes | No |
| Request/response logging | Yes | Yes |
| Cancellation support | No | Yes |
| Returns task handle | No | Yes (`NSURLSessionDataTask`) |
