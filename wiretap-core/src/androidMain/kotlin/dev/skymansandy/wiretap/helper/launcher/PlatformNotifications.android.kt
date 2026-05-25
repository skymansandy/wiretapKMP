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

internal actual fun platformOnNewHttpLog(httpLog: HttpLog) {
    WiretapNotificationManager.notifyHttpLog(WiretapContextProvider.context, httpLog)
}

internal actual fun platformOnDeleteHttpLog(id: Long) {
    WiretapNotificationManager.removeHttpEntry(WiretapContextProvider.context, id)
}

internal actual fun platformOnClearHttpLogs() {
    WiretapNotificationManager.clearHttpNotifications(WiretapContextProvider.context)
}

internal actual fun platformOnNewSocketConnection(entry: SocketConnection) {
    WiretapNotificationManager.notifyNewSocket(WiretapContextProvider.context, entry)
}

internal actual fun platformOnNewSocketMessage(entry: SocketConnection, message: SocketMessage) {
    WiretapNotificationManager.notifySocketMessage(WiretapContextProvider.context, entry, message)
}

internal actual fun platformOnClearSocketLogs() {
    WiretapNotificationManager.clearSockets(WiretapContextProvider.context)
}

internal actual fun platformOnNewSseConnection(entry: SseConnection) {
    WiretapNotificationManager.notifyNewSse(WiretapContextProvider.context, entry)
}

internal actual fun platformOnNewSseEvent(entry: SseConnection, event: SseEvent) {
    WiretapNotificationManager.notifySseEvent(WiretapContextProvider.context, entry, event)
}

internal actual fun platformOnClearSseLogs() {
    WiretapNotificationManager.clearSse(WiretapContextProvider.context)
}
