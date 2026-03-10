package dev.skymansandy.wiretap.notification

import dev.skymansandy.wiretap.WiretapContextProvider
import dev.skymansandy.wiretap.model.NetworkLogEntry

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) {
    WiretapNotificationManager.onNewEntry(WiretapContextProvider.context, entry)
}

internal actual fun onNetworkLogsCleared() {
    WiretapNotificationManager.clearAll(WiretapContextProvider.context)
}
