/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class WiretapShapes(
    val xs: Shape,
    val sm: Shape,
    val md: Shape,
    val lg: Shape,
    val xl: Shape,
    val pill: Shape,
    val xsRadius: Dp,
    val smRadius: Dp,
    val mdRadius: Dp,
    val lgRadius: Dp,
    val xlRadius: Dp,
)

internal val DefaultWiretapShapes = WiretapShapes(
    xs = RoundedCornerShape(4.dp),
    sm = RoundedCornerShape(6.dp),
    md = RoundedCornerShape(10.dp),
    lg = RoundedCornerShape(14.dp),
    xl = RoundedCornerShape(20.dp),
    pill = RoundedCornerShape(percent = 50),
    xsRadius = 4.dp,
    smRadius = 6.dp,
    mdRadius = 10.dp,
    lgRadius = 14.dp,
    xlRadius = 20.dp,
)
