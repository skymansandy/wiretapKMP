# URLSession — HTTP Logging

## Two APIs

`WiretapURLSessionInterceptor` provides two ways to make requests:

### `intercept()` — Full Rule Support

Fire-and-forget execution with mock and throttle rule support:

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
- No task object returned — cannot cancel

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
| HTTP logging | ✅ | ✅ |
| Mock rules | ✅ | — |
| Throttle rules | ✅ | — |
| Cancel support | — | ✅ |
| Returns task | — | ✅ |
| Auto-executes | ✅ | — |

Use `intercept()` when you need rule support (testing, development).
Use `dataTask()` when you need task control (cancellation, progress).

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

## How It Works

### `intercept()` Flow

1. If `enabled = false`, executes request directly (no logging)
2. Initializes session if needed (clears logs for `AppSession` retention)
3. Extracts request metadata (URL, method, headers, body)
4. Evaluates mock/throttle rules via `FindMatchingRuleUseCase`
5. Applies log retention (prunes old entries for `Days` retention)
6. Logs request to database (if `shouldLog` passes)
7. **Mock rule**: calls completion handler with mock data immediately
8. **Throttle rule**: delays via `dispatch_after` on GCD global queue, then executes
9. **No rule**: executes request immediately
10. Logs response when complete

### `dataTask()` Flow

1. If `enabled = false`, returns raw `session.dataTaskWithRequest()`
2. Extracts request metadata and logs request
3. Wraps completion handler to capture response
4. Returns the data task (caller must `resume()`)

## Error Handling

| Scenario | Response Code | Response Body |
|----------|:------------:|---------------|
| Network error | `0` | `NSError.localizedDescription` |
| In-progress | `-2` | — |

## Debug vs Release

Set `config.enabled = false` in release builds to disable logging and rule evaluation. When disabled, requests pass through directly to `NSURLSession`.

```swift
#if DEBUG
config.enabled = true
#else
config.enabled = false
#endif
```
