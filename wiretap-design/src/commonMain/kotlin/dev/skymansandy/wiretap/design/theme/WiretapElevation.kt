/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadow tokens. The CSS source uses `box-shadow` blur/y-offset/alpha triples
 * (e.g. `0 12px 32px rgba(0,0,0,0.6)`); on Compose we collapse these to the
 * equivalent `Modifier.shadow(elevation)` heights — the platform picks the
 * blur curve for us. Most flat elements (rows, headers) use borders not shadow.
 */
@Immutable
data class WiretapElevation(
    val menu: Dp,
    val dialog: Dp,
    val fab: Dp,
    val bezel: Dp,
)

internal val DefaultWiretapElevation = WiretapElevation(
    menu = 12.dp,
    dialog = 24.dp,
    fab = 8.dp,
    bezel = 40.dp,
)
