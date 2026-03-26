package dev.skymansandy.wiretap.navigation

import androidx.compose.runtime.compositionLocalOf
import dev.skymansandy.wiretap.ui.navigation.WiretapNavigator

internal val LocalWiretapNavigator = compositionLocalOf<WiretapNavigator> {
    error("No WiretapNavigator provided")
}
