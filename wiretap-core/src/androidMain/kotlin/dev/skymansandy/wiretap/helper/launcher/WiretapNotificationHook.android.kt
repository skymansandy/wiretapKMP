/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager

internal actual fun onNewHttpLog(httpLog: HttpLog) {
    WiretapNotificationManager.notifyHttpLog(WiretapContextProvider.context, httpLog)
}

internal actual fun onDeleteHttpLog(id: Long) {
    WiretapNotificationManager.removeHttpEntry(WiretapContextProvider.context, id)
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

internal actual fun onNewSseConnection(entry: SseConnection) {
    WiretapNotificationManager.notifyNewSse(WiretapContextProvider.context, entry)
}

internal actual fun onNewSseEvent(entry: SseConnection, event: SseEvent) {
    WiretapNotificationManager.notifySseEvent(WiretapContextProvider.context, entry, event)
}

internal actual fun onClearSseLogs() {
    WiretapNotificationManager.clearSse(WiretapContextProvider.context)
}
