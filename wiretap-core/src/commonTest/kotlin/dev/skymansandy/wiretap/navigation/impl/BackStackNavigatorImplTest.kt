/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.impl

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class BackStackNavigatorImplTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("push") {
        it("appends a screen on top of the stack") {
            val stack = newStack(WiretapScreen.HomeScreen)
            val nav = BackStackNavigatorImpl(stack)

            nav.push(WiretapScreen.HttpDetailScreen(entryId = 1))

            stack.size shouldBe 2
            stack[1] shouldBe WiretapScreen.HttpDetailScreen(entryId = 1)
        }
    }

    describe("pop") {
        it("removes top entry when more than one entry exists") {
            val stack = newStack(WiretapScreen.HomeScreen, WiretapScreen.HttpDetailScreen(1))
            val nav = BackStackNavigatorImpl(stack)

            nav.pop()

            stack.size shouldBe 1
            (stack[0] === WiretapScreen.HomeScreen) shouldBe true
        }

        it("is a no-op when only one entry remains") {
            val stack = newStack(WiretapScreen.HomeScreen)
            val nav = BackStackNavigatorImpl(stack)

            nav.pop()

            stack.size shouldBe 1
        }
    }

    describe("replaceTop") {
        it("swaps the top entry") {
            val stack = newStack(WiretapScreen.HomeScreen, WiretapScreen.HttpDetailScreen(1))
            val nav = BackStackNavigatorImpl(stack)

            nav.replaceTop(WiretapScreen.HttpDetailScreen(99))

            stack.size shouldBe 2
            stack[1] shouldBe WiretapScreen.HttpDetailScreen(99)
        }

        it("on empty stack just adds the screen") {
            val stack = newStack()
            val nav = BackStackNavigatorImpl(stack)

            nav.replaceTop(WiretapScreen.HomeScreen)

            stack.size shouldBe 1
            (stack[0] === WiretapScreen.HomeScreen) shouldBe true
        }
    }

    describe("popUntil") {
        it("pops entries until the predicate is satisfied") {
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

        it("keeps a single entry even when predicate never matches") {
            val stack = newStack(
                WiretapScreen.HomeScreen,
                WiretapScreen.HttpDetailScreen(1),
            )
            val nav = BackStackNavigatorImpl(stack)

            nav.popUntil { false }

            stack.size shouldBe 1
        }
    }

    describe("pushDetailPane") {
        it("pops to the list pane before adding the new entry") {
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
    }

    describe("clearDetailPane") {
        it("pops back to the list pane") {
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
})

private fun newStack(vararg keys: NavKey): NavBackStack<NavKey> = NavBackStack(*keys)
