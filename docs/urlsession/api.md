# URLSession — API Reference

## WiretapURLSessionInterceptor

```kotlin
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
) : KoinComponent
```

iOS URLSession interceptor with full logging and rule support.

### Constructor

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `session` | `NSURLSession` | `.sharedSession` | The URLSession to use for network requests |
| `configure` | `WiretapConfig.() -> Unit` | `{}` | Configuration builder lambda |

### Swift Construction

```swift
let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = true
    config.logRetention = LogRetention.Days(days: 7)
    config.shouldLog = { url, method in KotlinBoolean(value: true) }
    config.headerAction = { key in HeaderAction.Keep.shared }
}
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

## dataTask(request)

Creates a data task with logging. No mock/throttle rules applied.

```kotlin
fun dataTask(
    request: NSURLRequest,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

Returns `NSURLSessionDataTask` — caller must call `resume()`.

---

## dataTask(url)

Convenience overload from a URL string.

```kotlin
fun dataTask(
    url: String,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

---

## API Comparison

| Feature | `intercept()` | `dataTask()` |
|---------|:------------:|:------------:|
| HTTP logging | ✅ | ✅ |
| Mock rules | ✅ | — |
| Throttle rules | ✅ | — |
| Cancel support | — | ✅ |
| Returns task | — | ✅ |
| Auto-executes | ✅ | — |

---

## No-op (wiretap-urlsession-noop)

```kotlin
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
)
```

| Method | Behavior |
|--------|----------|
| `intercept()` | `session.dataTaskWithRequest(request).resume()` |
| `dataTask(request)` | `session.dataTaskWithRequest(request, completionHandler)` |
| `dataTask(url)` | Creates `NSURLRequest` from URL, delegates to `dataTask(request)` |

- Does NOT extend `KoinComponent`
- Config parameter accepted but ignored
- Zero overhead — direct pass-through
