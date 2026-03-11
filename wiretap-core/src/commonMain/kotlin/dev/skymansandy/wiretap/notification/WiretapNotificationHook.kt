package dev.skymansandy.wiretap.notification

import dev.skymansandy.wiretap.model.NetworkLogEntry

internal expect fun onNetworkEntryLogged(entry: NetworkLogEntry)

internal expect fun onNetworkLogsCleared()
