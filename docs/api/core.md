# Core API Reference

## WiretapConfig

Configuration shared by all plugins.

```kotlin
class WiretapConfig {
    var enabled: Boolean = true
    var shouldLog: (url: String, method: String) -> Boolean = { _, _ -> true }
    var headerAction: (key: String) -> HeaderAction = { HeaderAction.Keep }
    var logRetention: LogRetention = LogRetention.Forever
}
```

## HeaderAction

Controls how headers are logged.

```kotlin
sealed interface HeaderAction {
    object Keep : HeaderAction
    object Skip : HeaderAction
    data class Mask(val mask: String = "***") : HeaderAction
}
```

**Extension:**

```kotlin
fun Map<String, String>.applyHeaderAction(
    headerAction: (key: String) -> HeaderAction
): Map<String, String>
```

## LogRetention

Controls log lifetime.

```kotlin
sealed interface LogRetention {
    object Forever : LogRetention
    object AppSession : LogRetention
    data class Days(val days: Int) : LogRetention
}
```

## HttpLogEntry

Represents a captured HTTP request/response.

```kotlin
data class HttpLogEntry(
    val id: Long = 0,
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val requestBody: String? = null,
    val responseCode: Int = RESPONSE_CODE_IN_PROGRESS,  // -2
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val durationMs: Long = 0,
    val durationNs: Long = 0,
    val source: ResponseSource = ResponseSource.Network,
    val timestamp: Long,
    val matchedRuleId: Long? = null,
    val protocol: String? = null,
    val remoteAddress: String? = null,
    val tlsProtocol: String? = null,
    val cipherSuite: String? = null,
    val certificateCn: String? = null,
    val issuerCn: String? = null,
    val certificateExpiry: String? = null,
) {
    val isInProgress: Boolean
    companion object { const val RESPONSE_CODE_IN_PROGRESS = -2 }
}
```

## SocketLogEntry

Represents a WebSocket connection.

```kotlin
data class SocketLogEntry(
    val id: Long = 0,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val status: SocketStatus = SocketStatus.Connecting,
    val closeCode: Int? = null,
    val closeReason: String? = null,
    val failureMessage: String? = null,
    val messageCount: Long = 0,
    val timestamp: Long,
    val closedAt: Long? = null,
    val protocol: String? = null,
    val remoteAddress: String? = null,
    val historyCleared: Boolean = false,
)
```

## SocketMessage

A single WebSocket message.

```kotlin
data class SocketMessage(
    val id: Long = 0,
    val socketId: Long,
    val direction: SocketMessageDirection,  // Sent | Received
    val contentType: SocketContentType,     // Text | Binary
    val content: String,
    val byteCount: Long,
    val timestamp: Long,
)
```

## ResponseSource

```kotlin
enum class ResponseSource { Network, Mock, Throttle }
```

## SocketStatus

```kotlin
enum class SocketStatus { Connecting, Open, Closing, Closed, Failed }
```

## WiretapDi

Accessor for Wiretap dependencies (via Koin).

```kotlin
object WiretapDi : KoinComponent {
    val orchestrator: WiretapOrchestrator
    val ruleRepository: RuleRepository
    val findMatchingRule: FindMatchingRuleUseCase
    val findConflictingRules: FindConflictingRulesUseCase
    fun setTestKoin(koin: Koin?)
}
```

## WiretapOrchestrator

Central coordinator for HTTP and WebSocket logging.

```kotlin
interface WiretapOrchestrator : HttpOrchestrator, SocketOrchestrator
```

### HttpOrchestrator

```kotlin
interface HttpOrchestrator {
    suspend fun logEntry(entry: HttpLogEntry)
    suspend fun logRequest(entry: HttpLogEntry): Long
    suspend fun updateEntry(entry: HttpLogEntry)
    fun getAllLogs(): Flow<List<HttpLogEntry>>
    fun getPagedLogs(query: String): Flow<PagingData<HttpLogEntry>>
    suspend fun getLogById(id: Long): HttpLogEntry?
    suspend fun deleteLog(id: Long)
    suspend fun clearLogs()
    suspend fun purgeLogsOlderThan(cutoffMs: Long)
}
```

### SocketOrchestrator

```kotlin
interface SocketOrchestrator {
    suspend fun openSocketConnection(entry: SocketLogEntry): Long
    suspend fun updateSocketConnection(entry: SocketLogEntry)
    suspend fun logSocketMessage(message: SocketMessage)
    suspend fun getSocketById(id: Long): SocketLogEntry?
    fun getSocketByIdFlow(id: Long): Flow<SocketLogEntry?>
    fun getSocketMessages(socketId: Long): Flow<List<SocketMessage>>
    fun getAllSocketLogs(): Flow<List<SocketLogEntry>>
    fun getPagedSocketLogs(query: String): Flow<PagingData<SocketLogEntry>>
    suspend fun clearSocketLogs()
}
```

## Platform Functions

```kotlin
// Launch the Wiretap inspector UI
expect fun startWiretap()

// Enable shake-to-open gesture (Android only)
expect fun enableWiretapLauncher()
```
