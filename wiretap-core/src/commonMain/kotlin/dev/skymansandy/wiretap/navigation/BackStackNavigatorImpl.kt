package dev.skymansandy.wiretap.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.ui.screens.WiretapScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.HomeScreen

internal class BackStackNavigatorImpl(
    private val backStack: NavBackStack<NavKey>,
) : WiretapNavigator {

    override fun push(screen: WiretapScreen) {
        backStack.add(screen)
    }

    override fun pushDetailPane(screen: WiretapScreen) {
        backStack.removeAll { it !is HomeScreen }
        backStack.add(screen)
    }

    override fun clearDetailPane() {
        backStack.removeAll { it !is HomeScreen }
    }

    override fun pop() {
        backStack.removeLastOrNull()
    }

    override fun replaceTop(screen: WiretapScreen) {
        backStack.removeLastOrNull()
        backStack.add(screen)
    }
}
