package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageType

internal object NotificationFormatUtil {

    fun formatHttpEntry(entry: HttpLog): String {
        val status = when {
            entry.responseCode == HttpLog.RESPONSE_CODE_IN_PROGRESS -> "..."
            entry.responseCode > 0 -> entry.responseCode.toString()
            entry.responseCode == -1 -> "!!!"
            else -> "ERR"
        }

        return "${entry.method}  $status  ${entry.url}"
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
