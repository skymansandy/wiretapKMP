# OkHttp — Migration Guide

## RC9 → RC10

### `WiretapConfig` → `WiretapHttpConfig`

Renamed for clarity. Config classes are now in protocol-specific subpackages:

```diff
- import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
- import dev.skymansandy.wiretap.domain.model.config.HeaderAction
- import dev.skymansandy.wiretap.domain.model.config.LogRetention
+ import dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig
+ import dev.skymansandy.wiretap.domain.model.config.http.HeaderAction
+ import dev.skymansandy.wiretap.domain.model.config.http.LogRetention
```

```diff
- WiretapOkHttpInterceptor { // this: WiretapConfig
+ WiretapOkHttpInterceptor { // this: WiretapHttpConfig
      enabled = true
  }
```

### Package changes

Interceptor and listener imports moved to subpackages:

```diff
- import dev.skymansandy.wiretap.okhttp.WiretapOkHttpInterceptor
+ import dev.skymansandy.wiretap.okhttp.http.WiretapOkHttpInterceptor
```

### WebSocket — `wiretapped()` only

`WiretapOkHttpWebSocketListener` is now `internal`. Use the `wiretapped()` extension instead of the constructor:

```diff
- import dev.skymansandy.wiretap.okhttp.WiretapOkHttpWebSocketListener
- val listener = WiretapOkHttpWebSocketListener(myListener)
- client.newWebSocket(request, listener)
+ import dev.skymansandy.wiretap.okhttp.ws.wiretapped
+ client.newWebSocket(request, myListener.wiretapped { enabled = true })
```

The `wiretapped(enabled: Boolean)` overload is deprecated — use the config DSL:

```diff
- myListener.wiretapped(enabled = true)
+ myListener.wiretapped { enabled = true }
```

### SSE — new experimental API

SSE logging is new in RC10 and marked `@ExperimentalWiretapSseApi`. Add `@OptIn` at the call site:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
factory.newEventSource(request, myListener.wiretapped())
```

## RC13 → RC14

### Binary WebSocket frames now auto-decode as text

Binary frames that look like UTF-8 text are now rendered as text instead of `[Binary: N bytes]`. This is the default and requires no changes — libraries that ship text-over-binary (e.g. SignalRKore) become readable automatically.

To opt out or change the strategy, use the new `binaryDecoding` option:

```kotlin
client.newWebSocket(request, myListener.wiretapped {
    binaryDecoding = BinaryFrameDecoding.Placeholder  // pre-RC14 behaviour
    // = BinaryFrameDecoding.Utf8                     // always decode, even invalid sequences
    // = BinaryFrameDecoding.Custom { bytes -> ... }  // your own renderer
})
```
