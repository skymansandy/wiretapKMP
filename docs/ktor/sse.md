# Ktor — SSE Logging

## Setup

Install the Ktor SSE plugin and the Wiretap SSE plugin:

```kotlin
val client = HttpClient {
    install(SSE)
    install(WiretapKtorSsePlugin)    // SSE logging
    install(WiretapKtorHttpPlugin)   // HTTP logging
}
```

## Session Wrapping

Wrap your SSE session with `wiretapped()` to log incoming events. This creates a connection entry in Wiretap and returns a logging wrapper that intercepts all incoming events:

```kotlin
client.sse("https://example.com/events") {
    val session = this.wiretapped()

    session.incoming.collect { event ->
        println("Event: ${event.event} — ${event.data}")
    }
}
```

The `wiretapped()` extension is available on `ClientSSESession` — the standard Ktor SSE session type.

## WiretapSseSession API

| Property | Type | Description |
|----------|------|-------------|
| `call` | `HttpClientCall` | The underlying HTTP call for this SSE connection |
| `incoming` | `Flow<ServerSentEvent>` | Incoming events with automatic logging |

## How It Works

1. **`wiretapped()`** creates a connection entry via `SseLogManager` with status `Open`
2. Returns a `LoggingSseSession` that wraps the Ktor `ClientSSESession`
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
val client = HttpClient {
    install(SSE)
    install(WiretapKtorSsePlugin)
    install(WiretapKtorHttpPlugin)
}

client.sse("https://api.example.com/stream") {
    val session = this.wiretapped()

    session.incoming.collect { event ->
        when (event.event) {
            "message" -> handleMessage(event.data)
            "heartbeat" -> { /* ignore */ }
            else -> println("Unknown event: ${event.event}")
        }
    }
}
```

All SSE events are automatically captured — they appear in the SSE tab of the inspector UI.
