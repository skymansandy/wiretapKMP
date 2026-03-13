package dev.skymansandy.wiretap.util

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry

internal expect fun shareNetworkLog(subject: String, text: String)

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

internal fun buildCurlCommand(entry: NetworkLogEntry): String = buildString {
    append("curl -X ${entry.method} '${entry.url}'")
    entry.requestHeaders.forEach { (k, v) ->
        append(" \\\n  -H '$k: $v'")
    }
    if (!entry.requestBody.isNullOrEmpty()) {
        append(" \\\n  --data-raw '${entry.requestBody}'")
    }
}
