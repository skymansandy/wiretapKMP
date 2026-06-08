/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType

internal const val SOCKET_LOG_FILE_NAME = "wiretap_socket_log.txt"

internal fun buildSocketShareText(
    connection: SocketConnection,
    messages: List<SocketMessage>,
): String = buildString {
    appendLine("WS ${connection.url}")
    appendLine("Status: ${connection.status.name}")
    appendLine("Opened: ${formatTime(connection.timestamp)}")
    connection.closedAt?.let { appendLine("Closed: ${formatTime(it)}") }
    connection.closeCode?.let { appendLine("Close Code: $it") }
    connection.closeReason?.let { appendLine("Close Reason: $it") }
    connection.failureMessage?.let { appendLine("Error: $it") }
    connection.protocol?.let { appendLine("Protocol: $it") }
    connection.remoteAddress?.let { appendLine("Remote Address: $it") }
    appendLine()
    appendLine("--- Request Headers ---")
    if (connection.requestHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        connection.requestHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Messages (${messages.size}) ---")
    if (messages.isEmpty()) {
        append("(none)")
    } else {
        messages.forEachIndexed { index, message ->
            append(formatMessageLine(message))
            if (index < messages.lastIndex) appendLine()
        }
    }
}

private fun formatMessageLine(message: SocketMessage): String {
    val time = formatTime(message.timestamp)
    val bytes = formatBytes(message.byteCount)
    return when (message.contentType) {
        SocketContentType.Text, SocketContentType.Binary -> {
            val arrow = if (message.direction == SocketMessageType.Sent) ">>" else "<<"
            val tag = if (message.direction == SocketMessageType.Sent) "SENT" else "RECV"
            "[$time] $arrow $tag [${message.contentType.name}, $bytes] ${message.content}"
        }

        SocketContentType.Ping,
        SocketContentType.Pong,
        SocketContentType.Close,
        -> {
            val suffix = message.content.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
            "[$time] -- ${message.contentType.name.uppercase()}$suffix"
        }
    }
}
