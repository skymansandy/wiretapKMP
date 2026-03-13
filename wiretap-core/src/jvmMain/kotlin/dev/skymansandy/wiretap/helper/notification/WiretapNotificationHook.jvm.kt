package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry

internal actual fun onNetworkEntryLogged(entry: NetworkLogEntry) = Unit

internal actual fun onNetworkLogsCleared() = Unit
