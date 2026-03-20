# Rules & Matchers API Reference

## WiretapRule

A rule that intercepts matching HTTP requests.

```kotlin
data class WiretapRule(
    val id: Long = 0,
    val method: String = "*",
    val urlMatcher: UrlMatcher? = null,
    val headerMatchers: List<HeaderMatcher> = emptyList(),
    val bodyMatcher: BodyMatcher? = null,
    val action: RuleAction,
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)
```

### Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `id` | `Long` | `0` | Database-generated ID |
| `method` | `String` | `"*"` | HTTP method or `"*"` for all |
| `urlMatcher` | `UrlMatcher?` | `null` | URL matching strategy |
| `headerMatchers` | `List<HeaderMatcher>` | `[]` | Header conditions (AND logic) |
| `bodyMatcher` | `BodyMatcher?` | `null` | Body matching strategy |
| `action` | `RuleAction` | required | Mock or Throttle action |
| `enabled` | `Boolean` | `true` | Whether rule is active |
| `createdAt` | `Long` | `0` | Creation timestamp (epoch ms) |

### Matching Logic

All criteria use **AND** logic. A rule matches when:

1. `method` is `"*"` OR equals the request method
2. `urlMatcher` is null OR matches the URL
3. All `headerMatchers` match (empty list = always matches)
4. `bodyMatcher` is null OR matches the body

---

## RuleAction

```kotlin
sealed class RuleAction {
    enum class Type { Mock, Throttle }
    abstract val type: Type
    val name: String  // "Mock" or "Throttle"
}
```

### RuleAction.Mock

Returns a fake response without network access.

```kotlin
data class Mock(
    val responseCode: Int = 200,
    val responseBody: String? = null,
    val responseHeaders: Map<String, String>? = null,
    val throttleDelayMs: Long? = null,
    val throttleDelayMaxMs: Long? = null,
) : RuleAction()
```

| Property | Description |
|----------|-------------|
| `responseCode` | HTTP status code (default 200) |
| `responseBody` | Response body string |
| `responseHeaders` | Response headers map |
| `throttleDelayMs` | Optional delay before returning mock |
| `throttleDelayMaxMs` | If set, random delay between min and max |

### RuleAction.Throttle

Delays the request before proceeding to real network.

```kotlin
data class Throttle(
    val delayMs: Long = 0,
    val delayMaxMs: Long? = null,
) : RuleAction()
```

| Property | Description |
|----------|-------------|
| `delayMs` | Minimum delay in milliseconds |
| `delayMaxMs` | If set, random delay between min and max |

---

## UrlMatcher

Matches request URLs. All matching is case-insensitive.

```kotlin
sealed interface UrlMatcher {
    val pattern: String
    val type: MatcherType
}
```

| Variant | Behavior |
|---------|----------|
| `UrlMatcher.Exact(pattern)` | URL equals pattern exactly |
| `UrlMatcher.Contains(pattern)` | URL contains pattern as substring |
| `UrlMatcher.Regex(pattern)` | URL matches regex |

---

## HeaderMatcher

Matches request headers. Key matching is case-insensitive.

```kotlin
sealed interface HeaderMatcher {
    val key: String
}
```

| Variant | Behavior |
|---------|----------|
| `HeaderMatcher.KeyExists(key)` | Header with key exists (any value) |
| `HeaderMatcher.ValueExact(key, value)` | Header value equals string exactly |
| `HeaderMatcher.ValueContains(key, value)` | Header value contains substring |
| `HeaderMatcher.ValueRegex(key, pattern)` | Header value matches regex |

---

## BodyMatcher

Matches request body content.

```kotlin
sealed interface BodyMatcher {
    val pattern: String
    val type: MatcherType
}
```

| Variant | Behavior |
|---------|----------|
| `BodyMatcher.Exact(pattern)` | Body equals pattern exactly |
| `BodyMatcher.Contains(pattern)` | Body contains pattern as substring |
| `BodyMatcher.Regex(pattern)` | Body matches regex |

---

## MatcherType

```kotlin
enum class MatcherType { Exact, Contains, Regex }
```

---

## RuleRepository

Interface for managing rules programmatically.

```kotlin
interface RuleRepository {
    suspend fun addRule(rule: WiretapRule)
    suspend fun updateRule(rule: WiretapRule)
    fun getAll(): Flow<List<WiretapRule>>
    fun search(query: String): Flow<List<WiretapRule>>
    suspend fun getById(id: Long): WiretapRule?
    fun getEnabledRules(): Flow<List<WiretapRule>>
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
}
```

Access via `WiretapDi.ruleRepository`.

---

## Use Cases

### FindMatchingRuleUseCase

Finds the first enabled rule matching a request.

```kotlin
suspend operator fun invoke(
    url: String,
    method: String,
    headers: Map<String, String> = emptyMap(),
    body: String? = null,
): WiretapRule?
```

### FindConflictingRulesUseCase

Detects rules that overlap with the given rule.

```kotlin
suspend operator fun invoke(rule: WiretapRule): List<WiretapRule>
```
