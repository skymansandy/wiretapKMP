/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.util

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Convert an OKLCH triple to an sRGB [Color]. L is the perceptual lightness on [0, 1],
 * C is chroma (≥ 0), h is the hue in degrees. Matches the CSS `oklch()` notation
 * used by the design tokens (e.g. `oklch(74% 0.135 230)` → `oklch(0.74, 0.135, 230)`).
 */
internal fun oklch(l: Float, c: Float, hDeg: Float, alpha: Float = 1f): Color {
    val hRad = (hDeg * PI / 180.0).toFloat()
    val a = c * cos(hRad)
    val b = c * sin(hRad)

    val lPrime = l + 0.3963377774f * a + 0.2158037573f * b
    val mPrime = l - 0.1055613458f * a - 0.0638541728f * b
    val sPrime = l - 0.0894841775f * a - 1.2914855480f * b

    val lLin = lPrime * lPrime * lPrime
    val mLin = mPrime * mPrime * mPrime
    val sLin = sPrime * sPrime * sPrime

    val rLin = +4.0767416621f * lLin - 3.3077115913f * mLin + 0.2309699292f * sLin
    val gLin = -1.2684380046f * lLin + 2.6097574011f * mLin - 0.3413193965f * sLin
    val bLin = -0.0041960863f * lLin - 0.7034186147f * mLin + 1.7076147010f * sLin

    return Color(
        red = linearToSrgb(rLin).coerceIn(0f, 1f),
        green = linearToSrgb(gLin).coerceIn(0f, 1f),
        blue = linearToSrgb(bLin).coerceIn(0f, 1f),
        alpha = alpha.coerceIn(0f, 1f),
    )
}

private fun linearToSrgb(x: Float): Float =
    if (x <= 0.0031308f) 12.92f * x else 1.055f * x.toDouble().pow(1.0 / 2.4).toFloat() - 0.055f
