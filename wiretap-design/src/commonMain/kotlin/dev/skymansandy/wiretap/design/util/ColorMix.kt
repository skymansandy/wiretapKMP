/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.util

import androidx.compose.ui.graphics.Color

/**
 * Approximation of CSS `color-mix(in oklch, A pct%, transparent)` — produces a
 * translucent variant of [this] at the given fraction [pct] (0..1) by lowering alpha.
 * Used for the soft / line fills (e.g. selection backgrounds, badge tints) that the
 * design CSS expresses as `color-mix(in oklch, var(--m) 18%, transparent)`.
 */
internal fun Color.softened(pct: Float): Color = copy(alpha = pct.coerceIn(0f, 1f))
