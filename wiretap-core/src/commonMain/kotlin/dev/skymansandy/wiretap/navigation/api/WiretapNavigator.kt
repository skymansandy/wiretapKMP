/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.api

internal interface WiretapNavigator {

    fun push(screen: WiretapScreen)

    fun pushDetailPane(screen: WiretapScreen)

    fun clearDetailPane()

    fun pop()

    fun popUntil(predicate: (WiretapScreen) -> Boolean)

    fun replaceTop(screen: WiretapScreen)

    /**
     * Leaves the console entirely, handing control back to the host app.
     *
     * Unlike [pop] this is terminal — it dismisses the whole Wiretap surface
     * (finishes the activity on Android, dismisses the modal on iOS, disposes
     * the window on desktop) rather than moving within the back stack.
     */
    fun exit()

    companion object {

        val NoOp = object : WiretapNavigator {

            override fun push(screen: WiretapScreen) = Unit
            override fun pushDetailPane(screen: WiretapScreen) = Unit
            override fun clearDetailPane() = Unit
            override fun pop() = Unit
            override fun popUntil(predicate: (WiretapScreen) -> Boolean) = Unit
            override fun replaceTop(screen: WiretapScreen) = Unit
            override fun exit() = Unit
        }
    }
}
