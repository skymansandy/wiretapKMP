# URLSession Interceptor API Reference

## WiretapURLSessionInterceptor

iOS URLSession interceptor with full logging and rule support.

```kotlin
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
) : KoinComponent
```

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

Fire-and-forget execution with **full mock/throttle rule support**.

```kotlin
fun intercept(
    request: NSURLRequest,
    completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
)
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `NSURLRequest` | The URL request to execute |
| `completionHandler` | `(NSData?, NSHTTPURLResponse?, NSError?) -> Unit` | Called with response data |

### Behavior

1. If `enabled = false`, executes request directly (no logging)
2. Evaluates mock/throttle rules
3. **Mock**: Calls completion with mock data immediately (no network)
4. **Throttle**: Delays via `dispatch_after` on GCD global queue, then executes
5. **No rule**: Executes request immediately
6. Logs request and response to database

### Swift Usage

```swift
interceptor.intercept(request: request) { data, response, error in
    guard let data = data, error == nil else { return }
    // handle response
}
```

---

## dataTask() (NSURLRequest)

Creates a data task with logging. **No mock/throttle rules applied.**

```kotlin
fun dataTask(
    request: NSURLRequest,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `request` | `NSURLRequest` | The URL request |
| `completionHandler` | Completion handler | Called with response data |

### Returns

`NSURLSessionDataTask` — caller must call `resume()`.

### Swift Usage

```swift
let task = interceptor.dataTask(request: request) { data, response, error in
    // handle response
}
task.resume()

// Cancel support
task.cancel()
```

---

## dataTask() (URL String)

Convenience overload that creates an `NSURLRequest` from a URL string.

```kotlin
fun dataTask(
    url: String,
    completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
): NSURLSessionDataTask
```

### Swift Usage

```swift
let task = interceptor.dataTask(url: "https://api.example.com/users") { data, response, error in
    // handle response
}
task.resume()
```

---

## API Comparison

| Feature | `intercept()` | `dataTask()` |
|---------|:------------:|:------------:|
| HTTP logging | Yes | Yes |
| Mock rules | Yes | No |
| Throttle rules | Yes | No |
| Cancel support | No | Yes (`task.cancel()`) |
| Returns task | No | Yes |
| Auto-executes | Yes | No (must call `resume()`) |

---

## No-op Module (wiretap-urlsession-noop)

### WiretapURLSessionInterceptor

```kotlin
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
    configure: WiretapConfig.() -> Unit = {},
) {
    companion object {
        val shared: WiretapURLSessionInterceptor  // lazy singleton
    }

    fun intercept(request, completionHandler)
    // Executes: session.dataTaskWithRequest(request).resume()

    fun dataTask(request, completionHandler): NSURLSessionDataTask
    // Returns: session.dataTaskWithRequest(request, completionHandler)

    fun dataTask(url, completionHandler): NSURLSessionDataTask
    // Creates NSURLRequest from URL, delegates to dataTask(request)
}
```

- Does NOT extend `KoinComponent`
- Config parameter accepted but ignored
- Zero overhead — direct pass-through to `NSURLSession`
