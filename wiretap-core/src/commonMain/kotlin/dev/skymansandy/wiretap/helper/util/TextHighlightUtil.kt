/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import dev.skymansandy.wiretap.ui.theme.WiretapColors

internal fun highlightText(
    text: String,
    query: String,
    activeRange: IntRange? = null,
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var cursor = 0
        var match = lowerText.indexOf(lowerQuery, cursor)
        while (match >= 0) {
            append(text.substring(cursor, match))
            val isActive = activeRange != null &&
                match == activeRange.first &&
                match + query.length - 1 == activeRange.last
            val background = if (isActive) {
                WiretapColors.SearchHighlightActiveBackground
            } else {
                WiretapColors.SearchHighlightBackground
            }
            withStyle(SpanStyle(background = background, color = Color.Black)) {
                append(text.substring(match, match + query.length))
            }
            cursor = match + query.length
            match = lowerText.indexOf(lowerQuery, cursor)
        }
        append(text.substring(cursor))
    }
}
