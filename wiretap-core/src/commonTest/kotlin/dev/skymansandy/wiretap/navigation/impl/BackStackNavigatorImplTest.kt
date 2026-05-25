/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.impl

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BackStackNavigatorImplTest {

    private fun newStack(vararg keys: NavKey): NavBackStack<NavKey> = NavBackStack(*keys)

    @Test
    fun `push appends a screen on top of the stack`() {
        val stack = newStack(WiretapScreen.HomeScreen)
        val nav = BackStackNavigatorImpl(stack)

        nav.push(WiretapScreen.HttpDetailScreen(entryId = 1))

        stack.size shouldBe 2
        stack[1] shouldBe WiretapScreen.HttpDetailScreen(entryId = 1)
    }

    @Test
    fun `pop removes top entry when more than one entry exists`() {
        val stack = newStack(WiretapScreen.HomeScreen, WiretapScreen.HttpDetailScreen(1))
        val nav = BackStackNavigatorImpl(stack)

        nav.pop()

        stack.size shouldBe 1
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
    }

    @Test
    fun `pop is a no-op when only one entry remains`() {
        val stack = newStack(WiretapScreen.HomeScreen)
        val nav = BackStackNavigatorImpl(stack)

        nav.pop()

        stack.size shouldBe 1
    }

    @Test
    fun `replaceTop swaps the top entry`() {
        val stack = newStack(WiretapScreen.HomeScreen, WiretapScreen.HttpDetailScreen(1))
        val nav = BackStackNavigatorImpl(stack)

        nav.replaceTop(WiretapScreen.HttpDetailScreen(99))

        stack.size shouldBe 2
        stack[1] shouldBe WiretapScreen.HttpDetailScreen(99)
    }

    @Test
    fun `replaceTop on empty stack just adds the screen`() {
        val stack = newStack()
        val nav = BackStackNavigatorImpl(stack)

        nav.replaceTop(WiretapScreen.HomeScreen)

        stack.size shouldBe 1
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
    }

    @Test
    fun `popUntil pops entries until the predicate is satisfied`() {
        val stack = newStack(
            WiretapScreen.HomeScreen,
            WiretapScreen.HttpDetailScreen(1),
            WiretapScreen.SelectRuleCriteriaSheet(logId = 1),
        )
        val nav = BackStackNavigatorImpl(stack)

        nav.popUntil { it is WiretapScreen.ListPane }

        stack.size shouldBe 1
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
    }

    @Test
    fun `popUntil keeps a single entry even when predicate never matches`() {
        val stack = newStack(
            WiretapScreen.HomeScreen,
            WiretapScreen.HttpDetailScreen(1),
        )
        val nav = BackStackNavigatorImpl(stack)

        nav.popUntil { false }

        stack.size shouldBe 1
    }

    @Test
    fun `pushDetailPane pops to the list pane before adding the new entry`() {
        val stack = newStack(
            WiretapScreen.HomeScreen,
            WiretapScreen.HttpDetailScreen(1),
            WiretapScreen.SelectRuleCriteriaSheet(logId = 1),
        )
        val nav = BackStackNavigatorImpl(stack)

        nav.pushDetailPane(WiretapScreen.HttpDetailScreen(42))

        stack.size shouldBe 2
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
        stack[1] shouldBe WiretapScreen.HttpDetailScreen(42)
    }

    @Test
    fun `clearDetailPane pops back to the list pane`() {
        val stack = newStack(
            WiretapScreen.HomeScreen,
            WiretapScreen.HttpDetailScreen(1),
        )
        val nav = BackStackNavigatorImpl(stack)

        nav.clearDetailPane()

        stack.size shouldBe 1
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
    }
}
