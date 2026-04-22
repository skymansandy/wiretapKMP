/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.ui.theme.WiretapColors

enum class SseStatus {
    Connecting,
    Open,
    Closed,
    Failed,
    ;

    val bgColor: Color
        get() = when (this) {
            Connecting -> WiretapColors.StatusBlue
            Open -> WiretapColors.StatusGreen
            Closed -> WiretapColors.StatusGray
            Failed -> WiretapColors.StatusRed
        }

    val label: String
        get() = when (this) {
            Connecting -> "Connecting"
            Open -> "Open"
            Closed -> "Closed"
            Failed -> "Failed"
        }
}
