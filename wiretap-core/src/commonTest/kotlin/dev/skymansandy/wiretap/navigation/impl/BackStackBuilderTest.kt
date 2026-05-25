/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.impl

import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BackStackBuilderTest {

    @Test
    fun `no deep link yields a single-entry stack with HomeScreen`() {
        val stack = buildSyntheticBackStack(deepLinkScreen = null)

        stack.size shouldBe 1
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
    }

    @Test
    fun `deep link target is pushed after HomeScreen`() {
        val target = WiretapScreen.HttpDetailScreen(entryId = 42)

        val stack = buildSyntheticBackStack(deepLinkScreen = target)

        stack.size shouldBe 2
        (stack[0] === WiretapScreen.HomeScreen) shouldBe true
        stack[1] shouldBe target
    }
}
