/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.compose

import androidx.compose.runtime.compositionLocalOf
import dev.skymansandy.wiretap.navigation.api.WiretapNavigator

internal val LocalWiretapNavigator = compositionLocalOf<WiretapNavigator> {
    error("No WiretapNavigator provided")
}
