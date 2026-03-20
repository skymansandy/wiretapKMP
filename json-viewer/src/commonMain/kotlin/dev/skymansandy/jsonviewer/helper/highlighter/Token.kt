package dev.skymansandy.jsonviewer.helper.highlighter

internal data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
)
