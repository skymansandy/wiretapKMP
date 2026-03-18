package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) {
    WiretapNotificationManager.onNewEntry(WiretapContextProvider.context, entry)
}

internal actual fun onNetworkLogsCleared() {
    WiretapNotificationManager.clearAll(WiretapContextProvider.context)
}

internal actual fun onSocketConnectionLogged(entry: SocketLogEntry) {
    WiretapNotificationManager.onNewSocketEntry(WiretapContextProvider.context, entry)
}

internal actual fun onSocketMessageLogged(entry: SocketLogEntry, message: SocketMessage) {
    WiretapNotificationManager.onNewSocketMessage(WiretapContextProvider.context, entry, message)
}

internal actual fun onSocketLogsCleared() {
    WiretapNotificationManager.clearAll(WiretapContextProvider.context)
}
