# Ktor — Migration Guide

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

The `install` DSL is unchanged — only the config type name changed:

```diff
- install(WiretapKtorHttpPlugin) { // this: WiretapConfig
+ install(WiretapKtorHttpPlugin) { // this: WiretapHttpConfig
      enabled = true
  }
```

### WebSocket config moved to core

`WiretapWsConfig` moved from the Ktor plugin package to the core module:

```diff
- import dev.skymansandy.wiretap.plugin.ws.WiretapWsConfig
+ import dev.skymansandy.wiretap.domain.model.config.ws.WiretapWsConfig
```

### SSE — new experimental API

SSE logging is new in RC10. Install the plugin and sessions are wrapped automatically:

```kotlin
val client = HttpClient {
    install(WiretapKtorSsePlugin) {
        enabled = true
    }
}

@OptIn(ExperimentalWiretapSseApi::class)
client.sse("https://example.com/events") {
    incoming.collect { event ->
        println("Event: ${event.data}")
    }
}
```

## RC11 → RC12

### `wiretapped()` removed for WebSocket and SSE

`WiretapKtorWebSocketPlugin` and `WiretapKtorSsePlugin` now wrap sessions automatically. Remove all `wiretapped()` calls:

```diff
  client.webSocket("wss://example.com/ws") {
-     val session = this.wiretapped()
-     session?.send(Frame.Text("Hello!"))
-     for (frame in (session?.incoming ?: incoming)) { ... }
+     send(Frame.Text("Hello!"))
+     for (frame in incoming) { ... }
  }

  client.sse("https://example.com/events") {
-     val session = this.wiretapped()
-     session.incoming.collect { event -> ... }
+     incoming.collect { event -> ... }
  }
```

The `wiretapped()` extension is now deprecated with `ERROR` level and simply returns `this`.

## RC13 → RC14

### Binary WebSocket frames now auto-decode as text

Binary frames that look like UTF-8 text are now rendered as text instead of `[Binary: N bytes]`. This is the default and requires no changes — libraries that ship text-over-binary (e.g. SignalRKore) become readable automatically.

To opt out or change the strategy, use the new `binaryDecoding` option:

```kotlin
install(WiretapKtorWebSocketPlugin) {
    binaryDecoding = BinaryFrameDecoding.Placeholder  // pre-RC14 behaviour
    // = BinaryFrameDecoding.Utf8                     // always decode, even invalid sequences
    // = BinaryFrameDecoding.Custom { bytes -> ... }  // your own renderer
}
```
