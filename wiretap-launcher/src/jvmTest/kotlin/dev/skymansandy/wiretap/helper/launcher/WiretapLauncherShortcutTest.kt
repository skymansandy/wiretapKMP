/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.awt.event.KeyEvent
import javax.swing.JLabel

class WiretapLauncherShortcutTest : DescribeSpec({

    fun keyEvent(
        id: Int = KeyEvent.KEY_PRESSED,
        modifiers: Int = KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK,
        keyCode: Int = KeyEvent.VK_D,
    ): KeyEvent = KeyEvent(JLabel(), id, 0L, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED)

    describe("isWiretapLauncherShortcut") {
        it("matches Ctrl+Shift+D on KEY_PRESSED") {
            isWiretapLauncherShortcut(keyEvent()) shouldBe true
        }

        it("rejects KEY_RELEASED for the same shortcut") {
            isWiretapLauncherShortcut(keyEvent(id = KeyEvent.KEY_RELEASED)) shouldBe false
        }

        it("rejects when Shift is missing") {
            isWiretapLauncherShortcut(
                keyEvent(modifiers = KeyEvent.CTRL_DOWN_MASK),
            ) shouldBe false
        }

        it("rejects when Ctrl is missing") {
            isWiretapLauncherShortcut(
                keyEvent(modifiers = KeyEvent.SHIFT_DOWN_MASK),
            ) shouldBe false
        }

        it("rejects a different key code") {
            isWiretapLauncherShortcut(keyEvent(keyCode = KeyEvent.VK_A)) shouldBe false
        }

        it("rejects an extra Alt modifier") {
            isWiretapLauncherShortcut(
                keyEvent(
                    modifiers = KeyEvent.CTRL_DOWN_MASK or
                        KeyEvent.SHIFT_DOWN_MASK or
                        KeyEvent.ALT_DOWN_MASK,
                ),
            ) shouldBe false
        }
    }
})
