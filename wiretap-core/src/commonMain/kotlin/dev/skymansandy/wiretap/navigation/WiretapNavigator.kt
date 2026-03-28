package dev.skymansandy.wiretap.navigation

import androidx.compose.runtime.compositionLocalOf
import dev.skymansandy.wiretap.ui.screens.WiretapScreen

internal val LocalWiretapNavigator = compositionLocalOf<WiretapNavigator> {
    error("No WiretapNavigator provided")
}

internal interface WiretapNavigator {

    fun push(screen: WiretapScreen)

    fun pushDetailPane(screen: WiretapScreen)

    fun clearDetailPane()

    fun pop()

    fun replaceTop(screen: WiretapScreen)

    companion object {
        val NoOp = object : WiretapNavigator {
            override fun push(screen: WiretapScreen) = Unit
            override fun pushDetailPane(screen: WiretapScreen) = Unit
            override fun clearDetailPane() = Unit
            override fun pop() = Unit
            override fun replaceTop(screen: WiretapScreen) = Unit
        }
    }
}
