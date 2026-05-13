# Ktor — SSE Logging

!!! warning "Experimental"
    SSE inspection is in **early preview** and under active development. APIs are marked with `@ExperimentalWiretapSseApi` and may change in future releases. We're looking for early feedback — please [open an issue](https://github.com/skymansandy/wiretapKMP/issues) if you run into problems or have suggestions.

## Setup

Install the Ktor SSE plugin and the Wiretap SSE plugin. SSE APIs require opt-in:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
val client = HttpClient {
    install(SSE)
    install(WiretapKtorSsePlugin)    // SSE logging (experimental)
    install(WiretapKtorHttpPlugin)   // HTTP logging
}
```

## Automatic Session Wrapping

`WiretapKtorSsePlugin` wraps SSE sessions automatically — no extra calls needed. Just use the session directly and all incoming events are logged:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
client.sse("https://example.com/events") {
    incoming.collect { event ->
        println("Event: ${event.event} — ${event.data}")
    }
}
```

## WiretapSseSession API

| Property | Type | Description |
|----------|------|-------------|
| `call` | `HttpClientCall` | The underlying HTTP call for this SSE connection |
| `incoming` | `Flow<ServerSentEvent>` | Incoming events with automatic logging |

## How It Works

1. **`WiretapKtorSsePlugin`** automatically wraps SSE sessions and creates a connection entry via `SseLogManager` with status `Open`
2. The wrapped `LoggingSseSession` intercepts the Ktor `ClientSSESession`
3. Every incoming event is logged as it flows through the `incoming` flow
4. When the flow completes (server close, cancellation, or error), the connection status is updated to `Closed` or `Failed`

## What Gets Logged

### Connection

- URL
- Request headers
- Status transitions (Open → Closed / Failed)
- Failure message (if applicable)
- Timestamps

### Events

- Event type (`event` field)
- Data payload
- Event ID (`id` field)
- Retry interval (`retry` field)
- Byte count
- Timestamp

## Complete Example

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
val client = HttpClient {
    install(SSE)
    install(WiretapKtorSsePlugin)
    install(WiretapKtorHttpPlugin)
}

@OptIn(ExperimentalWiretapSseApi::class)
client.sse("https://api.example.com/stream") {
    incoming.collect { event ->
        when (event.event) {
            "message" -> handleMessage(event.data)
            "heartbeat" -> { /* ignore */ }
            else -> println("Unknown event: ${event.event}")
        }
    }
}
```

All SSE events are automatically captured — they appear in the SSE tab of the inspector UI.
