/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import dev.skymansandy.wiretap.ui.theme.WiretapColors

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
) {

    val statusColor = when (status) {
        SocketStatus.Connecting -> WiretapColors.StatusBlue
        SocketStatus.Open -> WiretapColors.StatusGreen
        SocketStatus.Closing -> WiretapColors.StatusAmber
        SocketStatus.Closed -> WiretapColors.StatusGray
        SocketStatus.Failed -> WiretapColors.StatusRed
    }
}
