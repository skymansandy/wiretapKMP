package dev.skymansandy.wiretap.navigation.impl

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.navigation.api.WiretapNavigator
import dev.skymansandy.wiretap.navigation.api.WiretapScreen

internal class BackStackNavigatorImpl(
    private val backStack: NavBackStack<NavKey>,
) : WiretapNavigator {

    override fun push(screen: WiretapScreen) {
        backStack.add(screen)
    }

    override fun pushDetailPane(screen: WiretapScreen) {
        popUntil { it is WiretapScreen.ListPane }
        backStack.add(screen)
    }

    override fun clearDetailPane() {
        popUntil { it is WiretapScreen.ListPane }
    }

    override fun pop() {
        if (backStack.size > 1) backStack.removeLast()
    }

    override fun popUntil(predicate: (WiretapScreen) -> Boolean) {
        while (backStack.size > 1) {
            val last = backStack.lastOrNull() as? WiretapScreen ?: break
            if (predicate(last)) break
            backStack.removeLast()
        }
    }

    override fun replaceTop(screen: WiretapScreen) {
        backStack.removeLastOrNull()
        backStack.add(screen)
    }
}
