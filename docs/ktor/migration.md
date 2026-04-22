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

SSE logging is new in RC10. Install the plugin and use `wiretapped()` on your SSE session:

```kotlin
val client = HttpClient {
    install(WiretapKtorSsePlugin) {
        enabled = true
    }
}

@OptIn(ExperimentalWiretapSseApi::class)
client.sse("https://example.com/events") {
    val session = this.wiretapped()
    session.incoming.collect { event ->
        println("Event: ${event.data}")
    }
}
```
