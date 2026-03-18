package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

interface NetworkLogger {
    fun log(entry: NetworkLogEntry)
    fun logSocket(entry: SocketLogEntry) {}
    fun logSocketMessage(message: SocketMessage) {}
}
