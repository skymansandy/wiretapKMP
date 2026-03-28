package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType

internal object NotificationFormatUtil {

    fun formatHttpEntry(entry: HttpLog): String {
        return "${entry.method}  ${entry.statusText}  ${entry.url}"
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

    fun socketUrlDisplay(url: String): String {
        val afterScheme = url.substringAfter("://")
        val host = afterScheme.substringBefore("/").substringBefore("?")
        val path = afterScheme.removePrefix(host).ifEmpty { "/" }
        return "$host$path"
    }
}
