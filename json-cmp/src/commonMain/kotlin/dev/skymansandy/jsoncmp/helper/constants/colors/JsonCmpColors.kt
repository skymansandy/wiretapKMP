package dev.skymansandy.jsoncmp.helper.constants.colors

import androidx.compose.ui.graphics.Color
import dev.skymansandy.jsoncmp.model.JsonPart

data class JsonCmpColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val booleanColor: Color,
    val nullColor: Color,
    val punctuation: Color,
    val lineNumber: Color,
    val foldHint: Color,
    val background: Color,
    val gutterBackground: Color,
    val highlight: Color,
    val highlightFg: Color,
    val gutterBorder: Color,
    val foldEllipsis: Color,
    val errorBackground: Color,
    val errorForeground: Color,
) {

    companion object {
        val Dark = JsonCmpColors(
            key = Color(0xFF9CDCFE),
            string = Color(0xFFCE9178),
            number = Color(0xFFB5CEA8),
            booleanColor = Color(0xFF569CD6),
            nullColor = Color(0xFF808080),
            punctuation = Color(0xFFD4D4D4),
            lineNumber = Color(0xFF858585),
            foldHint = Color(0xFF858585),
            background = Color(0xFF1E1E1E),
            gutterBackground = Color(0xFF252526),
            highlight = Color(0xFFFFEB3B),
            highlightFg = Color(0xFF1E1E1E),
            gutterBorder = Color(0xFF3C3C3C),
            foldEllipsis = Color(0xFFC586C0),
            errorBackground = Color(0xFF5C2020),
            errorForeground = Color(0xFFFF6B6B),
        )

        val Light = JsonCmpColors(
            key = Color(0xFF0451A5),
            string = Color(0xFFA31515),
            number = Color(0xFF098658),
            booleanColor = Color(0xFF0000FF),
            nullColor = Color(0xFF808080),
            punctuation = Color(0xFF333333),
            lineNumber = Color(0xFF999999),
            foldHint = Color(0xFF999999),
            background = Color(0xFFFFFFFF),
            gutterBackground = Color(0xFFF3F3F3),
            highlight = Color(0xFFFFE082),
            highlightFg = Color(0xFF333333),
            gutterBorder = Color(0xFFE0E0E0),
            foldEllipsis = Color(0xFFAF00DB),
            errorBackground = Color(0xFFFFE0E0),
            errorForeground = Color(0xFFCC0000),
        )

        val Monokai = JsonCmpColors(
            key = Color(0xFFF92672),
            string = Color(0xFFE6DB74),
            number = Color(0xFFAE81FF),
            booleanColor = Color(0xFFAE81FF),
            nullColor = Color(0xFF75715E),
            punctuation = Color(0xFFF8F8F2),
            lineNumber = Color(0xFF90908A),
            foldHint = Color(0xFF90908A),
            background = Color(0xFF272822),
            gutterBackground = Color(0xFF2F3029),
            highlight = Color(0xFFFFE082),
            highlightFg = Color(0xFF272822),
            gutterBorder = Color(0xFF3E3D32),
            foldEllipsis = Color(0xFF66D9EF),
            errorBackground = Color(0xFF5C2020),
            errorForeground = Color(0xFFF92672),
        )

        val Dracula = JsonCmpColors(
            key = Color(0xFF8BE9FD),
            string = Color(0xFFF1FA8C),
            number = Color(0xFFBD93F9),
            booleanColor = Color(0xFFBD93F9),
            nullColor = Color(0xFF6272A4),
            punctuation = Color(0xFFF8F8F2),
            lineNumber = Color(0xFF6272A4),
            foldHint = Color(0xFF6272A4),
            background = Color(0xFF282A36),
            gutterBackground = Color(0xFF21222C),
            highlight = Color(0xFFFFB86C),
            highlightFg = Color(0xFF282A36),
            gutterBorder = Color(0xFF44475A),
            foldEllipsis = Color(0xFFFF79C6),
            errorBackground = Color(0xFF4D1F28),
            errorForeground = Color(0xFFFF5555),
        )

        val SolarizedDark = JsonCmpColors(
            key = Color(0xFF268BD2),
            string = Color(0xFF2AA198),
            number = Color(0xFFD33682),
            booleanColor = Color(0xFFCB4B16),
            nullColor = Color(0xFF657B83),
            punctuation = Color(0xFF839496),
            lineNumber = Color(0xFF586E75),
            foldHint = Color(0xFF586E75),
            background = Color(0xFF002B36),
            gutterBackground = Color(0xFF073642),
            highlight = Color(0xFFB58900),
            highlightFg = Color(0xFF002B36),
            gutterBorder = Color(0xFF094959),
            foldEllipsis = Color(0xFF6C71C4),
            errorBackground = Color(0xFF3B1518),
            errorForeground = Color(0xFFDC322F),
        )
    }
}

internal fun partColor(part: JsonPart, colors: JsonCmpColors): Color = when (part) {
    is JsonPart.Key -> colors.key
    is JsonPart.StrVal -> colors.string
    is JsonPart.NumVal -> colors.number
    is JsonPart.BoolVal -> colors.booleanColor
    is JsonPart.NullVal -> colors.nullColor
    is JsonPart.Punct -> colors.punctuation
    is JsonPart.Indent -> colors.punctuation
}
