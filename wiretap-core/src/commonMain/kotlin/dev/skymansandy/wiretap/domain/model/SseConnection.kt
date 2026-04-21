/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import dev.skymansandy.wiretap.ui.theme.WiretapColors

data class SseConnection(
    val id: Long = 0,
    val url: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val status: SseStatus = SseStatus.Connecting,
    val failureMessage: String? = null,
    val eventCount: Long = 0,
    val timestamp: Long,
    val closedAt: Long? = null,
    val lastEventId: String? = null,
    val retryMs: Long? = null,
    val historyCleared: Boolean = false,
) {

    val statusColor = when (status) {
        SseStatus.Connecting -> WiretapColors.StatusBlue
        SseStatus.Open -> WiretapColors.StatusGreen
        SseStatus.Closed -> WiretapColors.StatusGray
        SseStatus.Failed -> WiretapColors.StatusRed
    }
}
