package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) = Unit

internal actual fun onNetworkLogsCleared() = Unit

internal actual fun onSocketConnectionLogged(entry: SocketLogEntry) = Unit

internal actual fun onSocketMessageLogged(entry: SocketLogEntry, message: SocketMessage) = Unit

internal actual fun onSocketLogsCleared() = Unit
