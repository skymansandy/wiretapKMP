package dev.skymansandy.wiretap.util

internal fun looksLikeJson(text: String): Boolean {
    val t = text.trim()
    return (t.startsWith("{") && t.endsWith("}")) || (t.startsWith("[") && t.endsWith("]"))
}
