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

## Disabling for Release

Use `config.enabled = false` to disable all logging and rule evaluation. When disabled, requests pass through directly to `NSURLSession` with no overhead.

```swift
#if DEBUG
let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = true
}
#else
let interceptor = WiretapURLSessionInterceptor(session: .shared) { config in
    config.enabled = false
}
#endif
```
