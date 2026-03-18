package dev.skymansandy.wiretap.config

import dev.skymansandy.wiretap.domain.model.UrlMatcher

/**
 * A composable filter controlling which requests are captured by Wiretap.
 *
 * When multiple [RequestFilter]s are configured, ALL must match for a request to be captured
 * (AND logic). An empty filter list means all requests are captured.
 *
 * Example — capture only authenticated POST/PUT calls to the API:
 * ```kotlin
 * requestFilters = listOf(
 *     RequestFilter.ByUrl(UrlMatcher.Contains("/api/")),
 *     RequestFilter.ByMethod("POST", "PUT"),
 * )
 * ```
 */
sealed class RequestFilter {

    /**
     * Captures requests whose URL matches [matcher].
     * Supports [UrlMatcher.Exact], [UrlMatcher.Contains], and [UrlMatcher.Regex].
     */
    data class ByUrl(val matcher: UrlMatcher) : RequestFilter()

    /**
     * Captures requests whose HTTP method is one of [methods] (case-insensitive).
     * Passing multiple methods is an OR — e.g. `ByMethod("POST", "PUT")` captures either.
     */
    data class ByMethod(val methods: List<String>) : RequestFilter() {
        constructor(vararg methods: String) : this(methods.toList())
    }
}

internal fun List<RequestFilter>.matches(url: String, method: String): Boolean {
    if (isEmpty()) return true
    return all { filter ->
        when (filter) {
            is RequestFilter.ByUrl -> filter.matcher.matches(url)
            is RequestFilter.ByMethod -> filter.methods.any { it.equals(method, ignoreCase = true) }
        }
    }
}

private fun UrlMatcher.matches(url: String): Boolean = when (this) {
    is UrlMatcher.Exact -> url.equals(pattern, ignoreCase = true)
    is UrlMatcher.Contains -> url.contains(pattern, ignoreCase = true)
    is UrlMatcher.Regex -> Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(url)
}
