package dev.skymansandy.wiretap.config

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry

/**
 * Defines how a specific request/response header should be treated in logged data.
 *
 * Rules are applied to the *copy* that is stored and displayed — the original
 * request/response objects are never mutated.
 *
 * Header name matching is case-insensitive.
 *
 * Example:
 * ```kotlin
 * headerSanitizationRules = listOf(
 *     HeaderSanitizationRule.Remove("Cookie"),
 *     HeaderSanitizationRule.Mask("Authorization"),
 *     HeaderSanitizationRule.Mask("X-Api-Key", mask = "<redacted>"),
 * )
 * ```
 */
sealed class HeaderSanitizationRule {

    abstract val headerName: String

    /** Removes the header entirely from logged request/response data. */
    data class Remove(override val headerName: String) : HeaderSanitizationRule()

    /** Replaces the header value with [mask] in logged data. */
    data class Mask(
        override val headerName: String,
        val mask: String = "***",
    ) : HeaderSanitizationRule()
}

internal fun NetworkLogEntry.applySanitization(rules: List<HeaderSanitizationRule>): NetworkLogEntry {
    if (rules.isEmpty()) return this
    return copy(
        requestHeaders = requestHeaders.applyRules(rules),
        responseHeaders = responseHeaders.applyRules(rules),
    )
}

private fun Map<String, String>.applyRules(rules: List<HeaderSanitizationRule>): Map<String, String> {
    val result = toMutableMap()
    for (rule in rules) {
        when (rule) {
            is HeaderSanitizationRule.Remove -> result.keys
                .filter { it.equals(rule.headerName, ignoreCase = true) }
                .forEach { result.remove(it) }
            is HeaderSanitizationRule.Mask -> {
                val key = result.keys.firstOrNull { it.equals(rule.headerName, ignoreCase = true) }
                if (key != null) result[key] = rule.mask
            }
        }
    }
    return result
}
