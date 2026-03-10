package dev.skymansandy.wiretap.logger

import dev.skymansandy.wiretap.model.NetworkLogEntry

class NetworkLoggerImpl : NetworkLogger {

    override fun log(entry: NetworkLogEntry) {
        println(
            "[Wiretap] ${entry.method} ${entry.url} -> ${entry.responseCode} (${entry.durationMs}ms) [${entry.source}]",
        )
    }
}
