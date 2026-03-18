package dev.skymansandy.wiretap.data.db.entity

sealed interface ActivityEntry {
    val timestamp: Long

    data class Http(val entry: NetworkLogEntry) : ActivityEntry {
        override val timestamp: Long get() = entry.timestamp
    }

    data class Socket(val entry: SocketLogEntry) : ActivityEntry {
        override val timestamp: Long get() = entry.timestamp
    }
}
