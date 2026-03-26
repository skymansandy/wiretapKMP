package dev.skymansandy.wiretap.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.ui.screens.WiretapScreen
import dev.skymansandy.wiretap.ui.screens.WiretapScreen.HomeScreen

internal val LocalWiretapNavigator = compositionLocalOf<WiretapNavigator> {
    error("No WiretapNavigator provided")
}

internal interface WiretapNavigator {

    fun navigateTo(screen: WiretapScreen)

    fun navigateToDetail(screen: WiretapScreen)

    fun clearDetailPanes()

    fun pop()

    fun popAndNavigateTo(screen: WiretapScreen)

    fun replaceTop(screen: WiretapScreen)

    companion object {
        val NoOp = object : WiretapNavigator {
            override fun navigateTo(screen: WiretapScreen) = Unit
            override fun navigateToDetail(screen: WiretapScreen) = Unit
            override fun clearDetailPanes() = Unit
            override fun pop() = Unit
            override fun popAndNavigateTo(screen: WiretapScreen) = Unit
            override fun replaceTop(screen: WiretapScreen) = Unit
        }
    }
}

internal class BackStackNavigator(
    private val backStack: NavBackStack<NavKey>,
) : WiretapNavigator {

    override fun navigateTo(screen: WiretapScreen) {
        backStack.add(screen)
    }

    override fun navigateToDetail(screen: WiretapScreen) {
        backStack.removeAll { it !is HomeScreen }
        backStack.add(screen)
    }

    override fun clearDetailPanes() {
        backStack.removeAll { it !is HomeScreen }
    }

    override fun pop() {
        backStack.removeLastOrNull()
    }

    override fun popAndNavigateTo(screen: WiretapScreen) {
        backStack.removeLastOrNull()
        backStack.add(screen)
    }

    override fun replaceTop(screen: WiretapScreen) {
        backStack.removeLastOrNull()
        backStack.add(screen)
    }
}
