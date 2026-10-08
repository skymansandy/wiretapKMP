/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

internal fun looksLikeJson(text: String): Boolean {
    val t = text.trim()
    return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))
}

/**
 * Strips structural whitespace from a JSON document, leaving string literals untouched.
 * Non-JSON text is returned unchanged so other payload types are never corrupted.
 */
internal fun minifyJson(text: String): String {
    if (!looksLikeJson(text)) return text
    val out = StringBuilder(text.length)
    var inString = false
    var escaped = false
    for (ch in text) {
        when {
            escaped -> {
                out.append(ch)
                escaped = false
            }
            inString && ch == '\\' -> {
                out.append(ch)
                escaped = true
            }
            ch == '"' -> {
                out.append(ch)
                inString = !inString
            }
            inString -> out.append(ch)
            ch.isWhitespace() -> Unit
            else -> out.append(ch)
        }
    }
    return out.toString()
}
