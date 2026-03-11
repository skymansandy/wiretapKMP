package dev.skymansandy.wiretap.notification

import dev.skymansandy.wiretap.model.NetworkLogEntry

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) = Unit

internal actual fun onNetworkLogsCleared() = Unit
