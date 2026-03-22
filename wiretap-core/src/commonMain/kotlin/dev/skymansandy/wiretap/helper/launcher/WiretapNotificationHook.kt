package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

internal expect fun onNetworkEntryLogged(entry: HttpLogEntry)

internal expect fun onNetworkLogsCleared()

internal expect fun onSocketConnectionLogged(entry: SocketEntry)

internal expect fun onSocketMessageLogged(entry: SocketEntry, message: SocketMessage)

internal expect fun onSocketLogsCleared()
