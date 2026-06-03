/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class LogSource { Network, Mock, Throttled }

@Composable
@ReadOnlyComposable
fun LogSource.color(): Color {
    val c = WiretapDesign.colors
    return when (this) {
        LogSource.Network -> c.fg4
        LogSource.Mock -> c.mock
        LogSource.Throttled -> c.throttle
    }
}
