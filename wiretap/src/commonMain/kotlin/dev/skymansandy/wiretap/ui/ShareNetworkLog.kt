package dev.skymansandy.wiretap.ui

import dev.skymansandy.wiretap.model.NetworkLogEntry

internal expect fun shareNetworkLog(entry: NetworkLogEntry)

internal fun buildShareText(entry: NetworkLogEntry): String = buildString {
    appendLine("${entry.method} ${entry.responseCode}")
    appendLine(entry.url)
    appendLine("Duration: ${entry.durationMs}ms | Source: ${entry.source.name}")
    appendLine()
    appendLine("--- Request Headers ---")
    if (entry.requestHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        entry.requestHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Request Body ---")
    appendLine(entry.requestBody ?: "(none)")
    appendLine()
    appendLine("--- Response Headers ---")
    if (entry.responseHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        entry.responseHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Response Body ---")
    append(entry.responseBody ?: "(none)")
}
