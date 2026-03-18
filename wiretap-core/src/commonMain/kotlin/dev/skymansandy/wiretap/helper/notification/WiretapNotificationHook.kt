package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry

internal expect fun onNetworkEntryLogged(entry: NetworkLogEntry)

internal expect fun onNetworkLogsCleared()

internal expect fun onSocketConnectionLogged(entry: SocketLogEntry)

internal expect fun onSocketLogsCleared()
