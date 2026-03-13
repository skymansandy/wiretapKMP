package dev.skymansandy.wiretap.helper.logger

import dev.skymansandy.wiretap.data.db.entity.NetworkLogEntry

interface NetworkLogger {
    fun log(entry: NetworkLogEntry)
}
