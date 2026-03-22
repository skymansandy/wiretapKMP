package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

internal actual fun onNetworkEntryLogged(entry: HttpLogEntry) = Unit

internal actual fun onNetworkLogsCleared() = Unit

internal actual fun onSocketConnectionLogged(entry: SocketEntry) = Unit

internal actual fun onSocketMessageLogged(entry: SocketEntry, message: SocketMessage) = Unit

internal actual fun onSocketLogsCleared() = Unit
