# URLSession — API Reference

## WiretapSession

```kotlin
class WiretapSession(
    configuration: NSURLSessionConfiguration = NSURLSessionConfiguration.defaultSessionConfiguration,
    configure: WiretapConfig.() -> Unit = {},
)
```

Drop-in URLSession wrapper with Wiretap network inspection. Manages its own `NSURLSession` internally.

### Constructor

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `configuration` | `NSURLSessionConfiguration` | `.defaultSessionConfiguration` | Session configuration for timeouts, caching, etc. |
| `configure` | `WiretapConfig.() -> Unit` | `{}` | Configuration builder lambda |

### Swift Construction

```swift
let session = WiretapSession { config in
    #if DEBUG
    config.enabled = true
    #else
    config.enabled = false
    #endif
    config.logRetention = LogRetentionDays(days: 7)
    config.shouldLog = { url, method in KotlinBoolean(value: true) }
    config.headerAction = { key in HeaderActionKeep.shared }
}
```

### Custom Configuration

```swift
let config = URLSessionConfiguration.default
config.timeoutIntervalForRequest = 10

let session = WiretapSession(configuration: config) { config in
    config.enabled = true
}
```

---

## dataTask(request)

Creates a data task with logging. Caller must call `resume()`.

```kotlin
fun dataTask(
    request: NSURLRequest,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `NSURLRequest` | The URL request to execute |
| `completionHandler` | `(NSData?, NSURLResponse?, NSError?) -> Unit` | Called with response data |

- Captures full request/response body, headers, timing
- Detects and logs cancellation automatically
- Mock/throttle rules are NOT applied — use `intercept()` for rule support

---

## dataTask(url: String)

Convenience overload from a URL string.

```kotlin
fun dataTask(
    url: String,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

---

## dataTask(url: NSURL)

Convenience overload from an NSURL.

```kotlin
fun dataTask(
    url: NSURL,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

---

## intercept()

Fire-and-forget execution with full mock/throttle rule support.

```kotlin
fun intercept(
    request: NSURLRequest,
    completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `NSURLRequest` | The URL request to execute |
| `completionHandler` | `(NSData?, NSHTTPURLResponse?, NSError?) -> Unit` | Called with response data |

- Mock rules: completion called with mock data immediately (no network)
- Throttle rules: delayed via GCD `dispatch_after`, then executed
- No rule: executed immediately

---

## invalidateAndCancel()

Invalidates the session, cancelling all outstanding tasks.

```kotlin
fun invalidateAndCancel()
```

---

## finishTasksAndInvalidate()

Allows outstanding tasks to finish, then invalidates the session.

```kotlin
fun finishTasksAndInvalidate()
```

---

## API Comparison

| Feature | `dataTask()` | `intercept()` |
|---------|:------------:|:------------:|
| HTTP logging | ✅ | ✅ |
| Cancel support | ✅ | — |
| Returns task | ✅ | — |
| Mock rules | — | ✅ |
| Throttle rules | — | ✅ |
| Auto-executes | — | ✅ |

---

## Debug vs Release

Use `WiretapSession` in debug and plain `URLSession` in release:

```swift
#if DEBUG
import WiretapURLSession
let session = WiretapSession { config in config.enabled = true }
#else
let session = URLSession.shared
#endif
```

See [Setup — Create a Session](setup.md#create-a-session) for the full bridge pattern.

---

## WiretapURLSessionInterceptor (Advanced)

```kotlin
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
) : KoinComponent
```

Low-level interceptor for when you need to provide your own `NSURLSession` instance. Prefer `WiretapSession` for most use cases.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `session` | `NSURLSession` | `.sharedSession` | The URLSession to use for network requests |
| `configure` | `WiretapConfig.() -> Unit` | `{}` | Configuration builder lambda |

Provides the same `intercept()` and `dataTask()` methods as `WiretapSession`.
