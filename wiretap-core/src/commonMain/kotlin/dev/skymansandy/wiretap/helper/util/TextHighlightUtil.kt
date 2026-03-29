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

internal fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var cursor = 0
        var match = lowerText.indexOf(lowerQuery, cursor)
        while (match >= 0) {
            append(text.substring(cursor, match))
            withStyle(SpanStyle(background = WiretapColors.SearchHighlightBackground, color = Color.Black)) {
                append(text.substring(match, match + query.length))
            }
            cursor = match + query.length
            match = lowerText.indexOf(lowerQuery, cursor)
        }
        append(text.substring(cursor))
    }
}
