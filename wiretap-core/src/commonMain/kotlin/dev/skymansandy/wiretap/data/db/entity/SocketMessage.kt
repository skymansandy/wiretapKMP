package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection

data class SocketMessage(
    val id: Long = 0,
    val socketId: Long,
    val direction: SocketMessageDirection,
    val contentType: SocketContentType,
    val content: String,
    val byteCount: Long,
    val timestamp: Long,
)
