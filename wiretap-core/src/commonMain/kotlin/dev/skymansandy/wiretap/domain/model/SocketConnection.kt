package dev.skymansandy.wiretap.domain.model

data class SocketConnection(
    val id: Long = 0,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val status: SocketStatus = SocketStatus.Connecting,
    val closeCode: Int? = null,
    val closeReason: String? = null,
    val failureMessage: String? = null,
    val messageCount: Long = 0,
    val timestamp: Long,
    val closedAt: Long? = null,
    val protocol: String? = null,
    val remoteAddress: String? = null,
    val historyCleared: Boolean = false,
)
