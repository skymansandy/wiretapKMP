# OkHttp — SSE Logging

!!! warning "Experimental"
    SSE inspection is in **early preview** and under active development. APIs are marked with `@ExperimentalWiretapSseApi` and may change in future releases. We're looking for early feedback — please [open an issue](https://github.com/skymansandy/wiretapKMP/issues) if you run into problems or have suggestions.

## Setup

Use the `wiretapped()` extension on any `EventSourceListener`:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
val factory = EventSources.createFactory(client)
val source = factory.newEventSource(request, myListener.wiretapped())
```

### Full example

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WiretapOkHttpInterceptor())
    .build()

val listener = object : EventSourceListener() {
    override fun onOpen(eventSource: EventSource, response: Response) {
        println("Connected")
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        println("Event: $type — $data")
    }

    override fun onClosed(eventSource: EventSource) {
        println("Closed")
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        println("Failed: ${t?.message}")
    }
}

val request = Request.Builder().url("https://api.example.com/stream").build()
val factory = EventSources.createFactory(client)
@OptIn(ExperimentalWiretapSseApi::class)
factory.newEventSource(request, listener.wiretapped())
```

## How It Works

`WiretapOkHttpEventSourceListener` wraps all `EventSourceListener` callbacks:

| Callback | What's Logged |
|----------|--------------|
| `onOpen` | Connection opened (status: Open) with URL and request headers |
| `onEvent` | Event logged with type, data, ID, and byte count |
| `onClosed` | Status updated to Closed with timestamp |
| `onFailure` | Status updated to Failed with error message |

All events are delegated to your original listener after logging.

## What Gets Logged

### Connection

- URL
- Request headers
- Status transitions (Open → Closed / Failed)
- Failure message (if applicable)
- Timestamps

### Events

- Event type (`type` parameter)
- Data payload
- Event ID (`id` parameter)
- Byte count
- Timestamp

## Configuration

Use the config DSL overload to configure SSE logging:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
factory.newEventSource(request, myListener.wiretapped {
    enabled = BuildConfig.DEBUG
})
```

Or use the simple `enabled` parameter:

```kotlin
@OptIn(ExperimentalWiretapSseApi::class)
factory.newEventSource(request, myListener.wiretapped(enabled = false))
```

## No-op

The noop module (`wiretap-okhttp-noop`) provides the same `wiretapped()` extensions but delegates all callbacks directly without logging.
