# Header Actions

Header actions control how request and response headers are stored in the log database. The original request/response headers are **never mutated** — actions only affect logged data.

## HeaderAction Types

### `Keep`

Log the header as-is (default behavior).

```kotlin
headerAction = { HeaderAction.Keep }
```

### `Mask(mask: String = "***")`

Replace the header value with a mask string in logged data.

```kotlin
headerAction = { key ->
    if (key.equals("Authorization", ignoreCase = true))
        HeaderAction.Mask()       // Value becomes "***"
    else
        HeaderAction.Keep
}

// Custom mask
HeaderAction.Mask("REDACTED")    // Value becomes "REDACTED"
HeaderAction.Mask("[hidden]")    // Value becomes "[hidden]"
```

### `Skip`

Omit the header entirely from logged data.

```kotlin
headerAction = { key ->
    if (key.equals("Cookie", ignoreCase = true))
        HeaderAction.Skip         // Header not logged at all
    else
        HeaderAction.Keep
}
```

## Common Patterns

### Mask Authentication Headers

```kotlin
headerAction = { key ->
    when {
        key.equals("Authorization", ignoreCase = true) -> HeaderAction.Mask()
        key.equals("X-Api-Key", ignoreCase = true) -> HeaderAction.Mask()
        key.equals("Cookie", ignoreCase = true) -> HeaderAction.Skip
        key.equals("Set-Cookie", ignoreCase = true) -> HeaderAction.Skip
        else -> HeaderAction.Keep
    }
}
```

### Allowlist Pattern

Only log specific headers:

```kotlin
val allowedHeaders = setOf("content-type", "accept", "cache-control")

headerAction = { key ->
    if (key.lowercase() in allowedHeaders) HeaderAction.Keep
    else HeaderAction.Skip
}
```
