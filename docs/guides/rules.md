# Rules Engine

WiretapKMP includes a rules engine that can **mock responses** or **throttle requests** based on configurable matching criteria. Rules are stored in SQLite and can be managed via the built-in UI or programmatically.

## Rule Anatomy

A `WiretapRule` has:

- **Method** — HTTP method to match, or `"*"` for all methods
- **URL Matcher** — Pattern to match against the request URL
- **Header Matchers** — Conditions on request headers (all must match)
- **Body Matcher** — Pattern to match against the request body
- **Action** — What to do when the rule matches (Mock or Throttle)
- **Enabled** — Toggle without deleting

All criteria use **AND logic** — method AND URL AND headers AND body must all match. The first enabled matching rule wins.

## URL Matching

Three strategies for matching URLs:

```kotlin
// Exact match (case-insensitive)
UrlMatcher.Exact("https://api.example.com/users")

// Substring match (case-insensitive)
UrlMatcher.Contains("/api/users")

// Regex match
UrlMatcher.Regex("https://api\\.example\\.com/users/\\d+")
```

## Header Matching

Match on request headers (case-insensitive key matching):

```kotlin
// Header exists (any value)
HeaderMatcher.KeyExists("Authorization")

// Exact value match
HeaderMatcher.ValueExact("Content-Type", "application/json")

// Value contains substring
HeaderMatcher.ValueContains("Accept", "json")

// Value matches regex
HeaderMatcher.ValueRegex("User-Agent", ".*Android.*")
```

Multiple header matchers use AND logic — all must match.

## Body Matching

Match on request body content:

```kotlin
BodyMatcher.Exact("""{"action": "delete"}""")
BodyMatcher.Contains("delete")
BodyMatcher.Regex(""""action":\s*"delete"""")
```

## Mock Rules

Return a fake response without hitting the network:

```kotlin
val rule = WiretapRule(
    method = "GET",
    urlMatcher = UrlMatcher.Contains("/api/users"),
    action = RuleAction.Mock(
        responseCode = 200,
        responseBody = """
            {
                "users": [
                    {"id": 1, "name": "Alice"},
                    {"id": 2, "name": "Bob"}
                ]
            }
        """.trimIndent(),
        responseHeaders = mapOf(
            "Content-Type" to "application/json",
        ),
    ),
)
```

### Mock with Simulated Latency

Add artificial delay to mock responses:

```kotlin
RuleAction.Mock(
    responseCode = 200,
    responseBody = """{"status": "ok"}""",
    throttleDelayMs = 500,       // Fixed 500ms delay
    // throttleDelayMaxMs = 2000, // Or random between 500ms–2000ms
)
```

### Error Simulation

```kotlin
// Simulate server error
RuleAction.Mock(responseCode = 500, responseBody = "Internal Server Error")

// Simulate not found
RuleAction.Mock(responseCode = 404, responseBody = """{"error": "Not found"}""")
```

## Throttle Rules

Delay requests before they reach the network:

```kotlin
val rule = WiretapRule(
    urlMatcher = UrlMatcher.Contains("/api/"),
    action = RuleAction.Throttle(
        delayMs = 3000,  // Fixed 3-second delay
    ),
)
```

### Random Delay Range

```kotlin
RuleAction.Throttle(
    delayMs = 1000,      // Minimum 1 second
    delayMaxMs = 5000,   // Maximum 5 seconds (random within range)
)
```

!!! note
    Throttle rules still make the real network call — they just add delay before it.

## Managing Rules

### Programmatic API

```kotlin
val ruleRepository = WiretapDi.ruleRepository

// Add a rule
ruleRepository.addRule(rule)

// List all rules
val rules: Flow<List<WiretapRule>> = ruleRepository.getAll()

// Toggle a rule
ruleRepository.setEnabled(ruleId, enabled = false)

// Delete a rule
ruleRepository.deleteById(ruleId)

// Clear all rules
ruleRepository.deleteAll()
```

### Built-in UI

The Wiretap inspector includes a **Rules** tab where you can:

- Create rules with a multi-step form
- Set URL, header, and body matchers
- Choose Mock or Throttle action
- Configure response code, body, headers, and delay
- See conflict warnings for overlapping rules
- Enable/disable rules with a toggle
- Edit or delete existing rules

## Platform Support

| Feature | Ktor | OkHttp | URLSession |
|---------|:----:|:------:|:----------:|
| Mock rules | Yes | Yes | `intercept()` only |
| Throttle rules | Yes | Yes | `intercept()` only |
| Rule evaluation | Automatic | Automatic | Automatic |

!!! warning "URLSession `dataTask()` Limitation"
    The `dataTask()` API on `WiretapURLSessionInterceptor` provides logging only — rules are NOT evaluated. Use `intercept()` for full rule support.

## Conflict Detection

When creating rules, Wiretap checks for overlapping rules and warns you:

```kotlin
val conflicts = WiretapDi.findConflictingRules(newRule)
if (conflicts.isNotEmpty()) {
    // Warn: these existing rules may conflict
}
```

Two rules "conflict" when their matchers overlap — e.g., both match `GET /api/users` but with different actions.
