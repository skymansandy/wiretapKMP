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

/**
 * Whether a frame of this type carries text a user can meaningfully search.
 * Binary and control frames only ever render a synthesised placeholder, so
 * matching against them would highlight text that is not part of the payload.
 */
internal fun SocketContentType.isTextSearchable(): Boolean = when (this) {
    SocketContentType.Text -> true
    SocketContentType.Binary,
    SocketContentType.Ping,
    SocketContentType.Pong,
    SocketContentType.Close,
    -> false
}
