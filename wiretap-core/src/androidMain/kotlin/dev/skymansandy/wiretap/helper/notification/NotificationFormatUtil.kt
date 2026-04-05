/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType
import dev.skymansandy.wiretap.helper.util.formatUrlDisplay

internal object NotificationFormatUtil {

    fun formatHttpEntry(entry: HttpLog): String {
        return "${entry.statusText}  ${entry.method}  ${entry.url}"
    }

    fun formatSocketMessage(message: SocketMessage): String {
        val direction = if (message.direction == SocketMessageType.Sent) "▲" else "▼"
        val content = if (message.contentType == SocketContentType.Binary) {
            "[Binary: ${message.byteCount} B]"
        } else {
            message.content.take(100)
        }
        return "$direction $content"
    }

    fun socketUrlDisplay(url: String): String = formatUrlDisplay(url)
}
