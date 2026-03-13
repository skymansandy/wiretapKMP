package dev.skymansandy.wiretap.helper.notification

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry

internal expect fun onNetworkEntryLogged(entry: NetworkLogEntry)

internal expect fun onNetworkLogsCleared()
