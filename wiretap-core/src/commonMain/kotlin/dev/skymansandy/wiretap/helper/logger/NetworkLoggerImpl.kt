package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus

class NetworkLoggerImpl : NetworkLogger {

    override fun log(entry: NetworkLogEntry) {
        if (entry.isInProgress) {
            println("[Wiretap] ${entry.method} ${entry.url} -> ...")
            return
        }
        val duration = if (entry.durationNs > 0) formatNs(entry.durationNs) else "${entry.durationMs}ms"
        val protocol = entry.protocol?.let { " $it" } ?: ""
        val remote = entry.remoteAddress?.let { " @$it" } ?: ""
        println("[Wiretap] ${entry.method} ${entry.url} -> ${entry.responseCode} ($duration) [${entry.source}]$protocol$remote")
    }

    override fun logSocket(entry: SocketLogEntry) {
        when (entry.status) {
            SocketStatus.CONNECTING -> println("[Wiretap] WS CONNECTING ${entry.url}")
            SocketStatus.OPEN -> println("[Wiretap] WS OPEN ${entry.url}")
            SocketStatus.CLOSING -> println("[Wiretap] WS CLOSING ${entry.url}")
            SocketStatus.CLOSED -> println("[Wiretap] WS CLOSED ${entry.closeCode} \"${entry.closeReason ?: ""}\"")
            SocketStatus.FAILED -> println("[Wiretap] WS FAILED ${entry.url} ${entry.failureMessage ?: ""}")
        }
    }

    override fun logSocketMessage(message: SocketMessage) {
        val arrow = when (message.direction) {
            SocketMessageDirection.SENT -> "\u2191"
            SocketMessageDirection.RECEIVED -> "\u2193"
        }
        val preview = if (message.content.length > 80) message.content.take(80) + "..." else message.content
        println("[Wiretap] WS $arrow \"$preview\" (${formatBytes(message.byteCount)})")
    }

    private fun formatBytes(bytes: Long): String = when {
        bytes >= 1_048_576 -> "${bytes / 1_048_576} MB"
        bytes >= 1_024 -> "${bytes / 1_024} kB"
        else -> "$bytes B"
    }

    private fun formatNs(ns: Long): String = when {
        ns < 1_000L -> "${ns}ns"
        ns < 1_000_000L -> "${ns / 1_000}.${(ns % 1_000).toString().padStart(3, '0')}µs"
        ns < 1_000_000_000L -> "${ns / 1_000_000}.${((ns % 1_000_000) / 1_000).toString().padStart(3, '0')}ms"
        else -> "${ns / 1_000_000_000}.${((ns % 1_000_000_000) / 1_000_000).toString().padStart(3, '0')}s"
    }
}
