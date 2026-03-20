# Log Retention

Log retention controls how long captured network logs are stored in the SQLite database.

## Retention Policies

### `LogRetention.Forever`

**Default.** Logs are kept indefinitely until manually cleared.

```kotlin
logRetention = LogRetention.Forever
```

### `LogRetention.AppSession`

Clears all existing logs when the plugin first initializes (i.e., on the first request after app startup). Only logs from the current session are visible.

```kotlin
logRetention = LogRetention.AppSession
```

Ideal during development when you only care about the current debugging session.

### `LogRetention.Days(days: Int)`

Prunes entries older than `days` days on each new request capture.

```kotlin
logRetention = LogRetention.Days(7)   // Keep last 7 days
logRetention = LogRetention.Days(1)   // Keep last 24 hours
logRetention = LogRetention.Days(30)  // Keep last month
```

Uses an indexed timestamp query — no full table scans, so performance is consistent regardless of database size.

## How Pruning Works

| Policy | When | What Happens |
|--------|------|-------------|
| `Forever` | Never | Nothing pruned |
| `AppSession` | First request after app start | `orchestrator.clearLogs()` — deletes all HTTP and WebSocket logs |
| `Days(n)` | Each new request capture | `orchestrator.purgeLogsOlderThan(cutoff)` — deletes entries with timestamp before cutoff |
