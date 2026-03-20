package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

internal expect fun onNetworkEntryLogged(entry: NetworkLogEntry)

internal expect fun onNetworkLogsCleared()

internal expect fun onSocketConnectionLogged(entry: SocketLogEntry)

internal expect fun onSocketMessageLogged(entry: SocketLogEntry, message: SocketMessage)

internal expect fun onSocketLogsCleared()
