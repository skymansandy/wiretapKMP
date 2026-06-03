/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.math.abs

/**
 * Verifies the OKLCH→sRGB pipeline for the four design accents lands within
 * eyeball-distance of the values the showcase renders in a browser. We don't
 * pin exact channel values — the math is a numerical approximation of CSS
 * `oklch()` — but we do guard the qualitative properties that drive how the
 * accents feel (cool vs warm, primary channel ordering, alpha modulation).
 */
class WiretapAccentTest : StringSpec({

    "cyan accent decodes to a believable cyan" {
        val rgb = WiretapAccent.Cyan.toColors().base
        (rgb.blue > rgb.green) shouldBe true
        (rgb.green > rgb.red) shouldBe true
        rgb.alpha shouldBe 1f
    }

    "amber accent reads as warm" {
        val rgb = WiretapAccent.Amber.toColors().base
        (rgb.red > rgb.blue) shouldBe true
        (rgb.red > 0.6f) shouldBe true
    }

    "green accent reads as green" {
        val rgb = WiretapAccent.Green.toColors().base
        (rgb.green > rgb.red) shouldBe true
        (rgb.green > rgb.blue) shouldBe true
    }

    "violet accent reads as cool" {
        val rgb = WiretapAccent.Violet.toColors().base
        (rgb.blue > rgb.green) shouldBe true
        (rgb.red > rgb.green) shouldBe true
    }

    "soft and line variants only modulate alpha, not channels" {
        val colors = WiretapAccent.Cyan.toColors()
        abs(colors.soft.alpha - 0.14f) shouldBeNear 0f
        abs(colors.line.alpha - 0.35f) shouldBeNear 0f
        colors.soft.red shouldBeNear colors.base.red
        colors.soft.green shouldBeNear colors.base.green
        colors.soft.blue shouldBeNear colors.base.blue
    }

    "density maps to the documented row padding" {
        WiretapDensity.Compact.rowPaddingVertical.value shouldBe 8f
        WiretapDensity.Regular.rowPaddingVertical.value shouldBe 12f
        WiretapDensity.Comfy.rowPaddingVertical.value shouldBe 16f
    }
})

// Compose `Color` stores channels as half-precision floats internally, so
// alpha 0.14f / 0.35f round-trip with ~0.001 of error — looser tolerance reflects
// that platform reality, not loose math.
private infix fun Float.shouldBeNear(other: Float) {
    if (abs(this - other) > 0.01f) throw AssertionError("expected $this ≈ $other (Δ ${abs(this - other)})")
}
