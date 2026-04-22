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
