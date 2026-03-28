package dev.skymansandy.wiretap.navigation.impl

import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HomeScreen

/**
 * Builds the synthetic back stack for deep-link navigation following the Nav3 recipe pattern.
 * When a deep-link target is provided, the back stack is pre-populated with
 * `[HomeScreen, target]` so that pressing Back returns to the home screen.
 */
internal fun buildSyntheticBackStack(
    deepLinkScreen: WiretapScreen? = null,
): Array<NavKey> = buildList<NavKey> {
    add(HomeScreen)
    if (deepLinkScreen != null) add(deepLinkScreen)
}.toTypedArray()
