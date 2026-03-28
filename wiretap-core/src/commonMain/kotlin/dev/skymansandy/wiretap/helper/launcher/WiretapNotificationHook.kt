package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage

internal expect fun onNewHttpLog(httpLog: HttpLog)

internal expect fun onClearHttpLogs()

internal expect fun onNewSocketConnection(entry: SocketConnection)

internal expect fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage)

internal expect fun onClearSocketLogs()
