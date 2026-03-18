package dev.skymansandy.wiretap.data.db.entity

import dev.skymansandy.wiretap.domain.model.SocketStatus

data class SocketLogEntry(
    val id: Long = 0,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val status: SocketStatus = SocketStatus.CONNECTING,
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
