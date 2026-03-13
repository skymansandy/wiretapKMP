package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) {
    WiretapNotificationManager.onNewEntry(WiretapContextProvider.context, entry)
}

internal actual fun onNetworkLogsCleared() {
    WiretapNotificationManager.clearAll(WiretapContextProvider.context)
}
