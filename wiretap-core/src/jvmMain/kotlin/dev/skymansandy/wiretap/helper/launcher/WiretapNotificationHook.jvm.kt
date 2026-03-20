package dev.skymansandy.wiretap.helper.launcher

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

internal actual fun onNetworkEntryLogged(entry: HttpLogEntry) = Unit

internal actual fun onNetworkLogsCleared() = Unit

internal actual fun onSocketConnectionLogged(entry: SocketLogEntry) = Unit

internal actual fun onSocketMessageLogged(entry: SocketLogEntry, message: SocketMessage) = Unit

internal actual fun onSocketLogsCleared() = Unit
