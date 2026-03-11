package dev.skymansandy.wiretap.logger

import dev.skymansandy.wiretap.model.NetworkLogEntry

class NetworkLoggerImpl : NetworkLogger {

    override fun log(entry: NetworkLogEntry) {
        val duration = if (entry.durationNs > 0) formatNs(entry.durationNs) else "${entry.durationMs}ms"
        println("[Wiretap] ${entry.method} ${entry.url} -> ${entry.responseCode} ($duration) [${entry.source}]")
    }

    private fun formatNs(ns: Long): String = when {
        ns < 1_000L -> "${ns}ns"
        ns < 1_000_000L -> "${ns / 1_000}.${(ns % 1_000).toString().padStart(3, '0')}µs"
        ns < 1_000_000_000L -> "${ns / 1_000_000}.${((ns % 1_000_000) / 1_000).toString().padStart(3, '0')}ms"
        else -> "${ns / 1_000_000_000}.${((ns % 1_000_000_000) / 1_000_000).toString().padStart(3, '0')}s"
    }
}
