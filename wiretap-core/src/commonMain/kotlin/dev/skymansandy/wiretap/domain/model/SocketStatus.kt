/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.ui.theme.WiretapColors

enum class SocketStatus {
    Connecting,
    Open,
    Closing,
    Closed,
    Failed,
    ;

    val bgColor: Color
        get() = when (this) {
            Connecting -> WiretapColors.StatusBlue
            Open -> WiretapColors.StatusGreen
            Closing -> WiretapColors.StatusAmber
            Closed -> WiretapColors.StatusGray
            Failed -> WiretapColors.StatusRed
        }

    val label: String
        get() = when (this) {
            Connecting -> "Connecting"
            Open -> "Open"
            Closing -> "Closing"
            Closed -> "Closed"
            Failed -> "Failed"
        }
}

enum class SocketMessageType {
    Sent,
    Received,
}

enum class SocketContentType {
    Text,
    Binary,
    Ping,
    Pong,
    Close,
}
