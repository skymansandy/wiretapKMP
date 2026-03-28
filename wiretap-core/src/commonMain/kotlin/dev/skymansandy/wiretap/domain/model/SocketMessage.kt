package dev.skymansandy.wiretap.domain.model

data class SocketMessage(
    val id: Long = 0,
    val socketId: Long,
    val direction: SocketMessageType,
    val contentType: SocketContentType,
    val content: String,
    val byteCount: Long,
    val timestamp: Long,
)
