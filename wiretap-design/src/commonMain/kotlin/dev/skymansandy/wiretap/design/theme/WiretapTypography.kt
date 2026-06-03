/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.skymansandy.wiretap.design.resources.Res
import dev.skymansandy.wiretap.design.resources.ibm_plex_mono_bold
import dev.skymansandy.wiretap.design.resources.ibm_plex_mono_medium
import dev.skymansandy.wiretap.design.resources.ibm_plex_mono_regular
import dev.skymansandy.wiretap.design.resources.ibm_plex_mono_semibold
import dev.skymansandy.wiretap.design.resources.ibm_plex_sans_bold
import dev.skymansandy.wiretap.design.resources.ibm_plex_sans_medium
import dev.skymansandy.wiretap.design.resources.ibm_plex_sans_regular
import dev.skymansandy.wiretap.design.resources.ibm_plex_sans_semibold
import org.jetbrains.compose.resources.Font

@Immutable
data class WiretapTypography(
    val sans: FontFamily,
    val mono: FontFamily,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val micro: TextStyle,
    val monoRow: TextStyle,
    val monoMeta: TextStyle,
    val code: TextStyle,
    val wordmark: TextStyle,
)

@Composable
internal fun rememberWiretapTypography(): WiretapTypography {
    val sans = FontFamily(
        Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal),
        Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium),
        Font(Res.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.ibm_plex_sans_bold, FontWeight.Bold),
    )
    val mono = FontFamily(
        Font(Res.font.ibm_plex_mono_regular, FontWeight.Normal),
        Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium),
        Font(Res.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
        Font(Res.font.ibm_plex_mono_bold, FontWeight.Bold),
    )
    return remember(sans, mono) {
        WiretapTypography(
            sans = sans,
            mono = mono,
            title = TextStyle(
                fontFamily = sans,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.01).em,
                textDecoration = TextDecoration.None,
            ),
            body = TextStyle(
                fontFamily = sans,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal,
            ),
            label = TextStyle(
                fontFamily = sans,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.06.em,
            ),
            micro = TextStyle(
                fontFamily = mono,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.12.em,
            ),
            monoRow = TextStyle(
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
            ),
            monoMeta = TextStyle(
                fontFamily = mono,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.02.em,
            ),
            code = TextStyle(
                fontFamily = mono,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal,
            ),
            wordmark = TextStyle(
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.04.em,
            ),
        )
    }
}
