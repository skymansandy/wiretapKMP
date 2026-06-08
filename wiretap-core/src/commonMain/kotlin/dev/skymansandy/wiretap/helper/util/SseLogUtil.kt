/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent

internal const val SSE_LOG_FILE_NAME = "wiretap_sse_log.txt"

internal fun buildSseShareText(
    connection: SseConnection,
    events: List<SseEvent>,
): String = buildString {
    appendLine("SSE ${connection.url}")
    appendLine("Status: ${connection.status.name}")
    appendLine("Opened: ${formatTime(connection.timestamp)}")
    connection.closedAt?.let { appendLine("Closed: ${formatTime(it)}") }
    connection.failureMessage?.let { appendLine("Error: $it") }
    connection.lastEventId?.let { appendLine("Last Event ID: $it") }
    connection.retryMs?.let { appendLine("Retry: ${it}ms") }
    appendLine()
    appendLine("--- Request Headers ---")
    if (connection.requestHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        connection.requestHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Events (${events.size}) ---")
    if (events.isEmpty()) {
        append("(none)")
    } else {
        events.forEachIndexed { index, event ->
            append(formatEventBlock(event))
            if (index < events.lastIndex) appendLine()
        }
    }
}

private fun formatEventBlock(event: SseEvent): String = buildString {
    val time = formatTime(event.timestamp)
    val bytes = formatBytes(event.byteCount)
    val header = buildString {
        append("[$time]")
        event.eventType?.let { append(" event: $it") }
        event.eventId?.let { append(" id: $it") }
        append(" ($bytes)")
    }
    appendLine(header)
    append(event.data)
}
