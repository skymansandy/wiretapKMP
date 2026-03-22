package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

internal actual fun onNetworkEntryLogged(entry: HttpLogEntry) {
    WiretapNotificationManager.onNewEntry(WiretapContextProvider.context, entry)
}

internal actual fun onNetworkLogsCleared() {
    WiretapNotificationManager.clearHttpNotifications(WiretapContextProvider.context)
}

internal actual fun onSocketConnectionLogged(entry: SocketEntry) {
    WiretapNotificationManager.onNewSocketEntry(WiretapContextProvider.context, entry)
}

internal actual fun onSocketMessageLogged(entry: SocketEntry, message: SocketMessage) {
    WiretapNotificationManager.onNewSocketMessage(WiretapContextProvider.context, entry, message)
}

internal actual fun onSocketLogsCleared() {
    WiretapNotificationManager.clearSocketNotifications(WiretapContextProvider.context)
}
