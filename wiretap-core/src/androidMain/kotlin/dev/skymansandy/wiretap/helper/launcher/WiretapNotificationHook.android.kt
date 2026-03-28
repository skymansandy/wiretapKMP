package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager

internal actual fun onNewHttpLog(httpLog: HttpLog) {
    WiretapNotificationManager.notifyHttpLog(WiretapContextProvider.context, httpLog)
}

internal actual fun onClearHttpLogs() {
    WiretapNotificationManager.clearHttpNotifications(WiretapContextProvider.context)
}

internal actual fun onNewSocketConnection(entry: SocketConnection) {
    WiretapNotificationManager.notifyNewSocket(WiretapContextProvider.context, entry)
}

internal actual fun onNewSocketMessage(entry: SocketConnection, message: SocketMessage) {
    WiretapNotificationManager.notifySocketMessage(WiretapContextProvider.context, entry, message)
}

internal actual fun onClearSocketLogs() {
    WiretapNotificationManager.clearSockets(WiretapContextProvider.context)
}
