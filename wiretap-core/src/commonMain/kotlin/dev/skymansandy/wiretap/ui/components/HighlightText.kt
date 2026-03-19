package dev.skymansandy.wiretap.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

internal fun highlightText(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var cursor = 0
        var match = lowerText.indexOf(lowerQuery, cursor)
        while (match >= 0) {
            append(text.substring(cursor, match))
            withStyle(SpanStyle(background = Color(0xFFFFEB3B), color = Color.Black)) {
                append(text.substring(match, match + query.length))
            }
            cursor = match + query.length
            match = lowerText.indexOf(lowerQuery, cursor)
        }
        append(text.substring(cursor))
    }
}
