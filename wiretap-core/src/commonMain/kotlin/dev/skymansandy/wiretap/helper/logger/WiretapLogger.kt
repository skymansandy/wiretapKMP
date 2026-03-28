package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage

interface WiretapLogger {

    fun logHttp(entry: HttpLog)

    fun logSocket(entry: SocketConnection) = Unit

    fun logSocketMessage(message: SocketMessage) = Unit
}
