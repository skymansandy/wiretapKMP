package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

interface WiretapLogger {

    fun logHttp(entry: HttpLogEntry)

    fun logSocket(entry: SocketEntry) = Unit

    fun logSocketMessage(message: SocketMessage) = Unit
}
