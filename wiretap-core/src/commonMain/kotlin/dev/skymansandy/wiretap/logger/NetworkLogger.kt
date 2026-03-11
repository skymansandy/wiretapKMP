package dev.skymansandy.wiretap.logger

import dev.skymansandy.wiretap.model.NetworkLogEntry

interface NetworkLogger {
    fun log(entry: NetworkLogEntry)
}
