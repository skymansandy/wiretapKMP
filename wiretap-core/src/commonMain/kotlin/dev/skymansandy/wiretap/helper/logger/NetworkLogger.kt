package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

interface NetworkLogger {

    fun logHttp(entry: HttpLogEntry)

    fun logSocket(entry: SocketLogEntry) = Unit

    fun logSocketMessage(message: SocketMessage) = Unit
}
