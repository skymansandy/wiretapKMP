package dev.skymansandy.jsoncmp.model

internal data class JsonLine(
    val lineNumber: Int,
    val depth: Int,
    val parts: List<JsonPart>,
    val foldId: Int?,
    val foldType: FoldType?,
    val parentFoldIds: List<Int>,
    val foldChildCount: Int = 0,
    /** Inline text of all lines inside this fold (children + closing bracket),
     *  joined without newlines. Appended as transparent text when folded so
     *  copy/paste captures the real JSON content. */
    val foldedContent: String = "",
    /** Path from root to the node this line represents. Empty for closing brackets. */
    val path: JsonPath = emptyList(),
    /** Whether this line is a closing bracket (not a value node). */
    val isClosingBracket: Boolean = false,
)
