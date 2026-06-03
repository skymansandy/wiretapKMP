/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.design.theme.WiretapDesign
import dev.skymansandy.wiretap.design.util.oklch

/**
 * Tokenizing JSON viewer. Recognizes strings (yellow), object keys (blue),
 * numbers (green), booleans/null (violet); punctuation falls back to fg3.
 * Optional [searchQuery] highlights every match across all tokens with the
 * amber `.json mark` background from styles.css.
 */
@Composable
fun JsonViewer(
    json: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
) {
    val c = WiretapDesign.colors
    val mark = remember { oklch(0.76f, 0.16f, 75f, alpha = 0.40f) }
    val pretty = remember(json) { runCatching { prettifyJson(json) }.getOrDefault(json) }
    val lines = remember(pretty) { pretty.split('\n') }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface0)
            .padding(vertical = 10.dp),
    ) {
        Column {
            lines.forEachIndexed { i, line ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text(
                        text = (i + 1).toString(),
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg4,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(28.dp).padding(end = 12.dp),
                    )
                    Text(
                        text = tokenizeJsonLine(line, c, searchQuery, mark),
                        style = WiretapDesign.typography.code,
                        color = c.fg1,
                    )
                }
            }
        }
    }
}

private val TOKEN_REGEX = Regex(
    """("(?:\\.|[^"\\])*")(\s*:)?|\b(true|false|null)\b|(-?\d+\.?\d*)|([{}\[\],])""",
)

private fun tokenizeJsonLine(
    line: String,
    c: dev.skymansandy.wiretap.design.theme.WiretapColors,
    query: String,
    mark: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var pos = 0
    val colorString = oklch(0.76f, 0.13f, 80f)
    val colorKey = oklch(0.78f, 0.12f, 230f)
    val colorNumber = oklch(0.76f, 0.14f, 150f)
    val colorBool = oklch(0.76f, 0.14f, 320f)

    fun append(text: String, color: Color) {
        if (query.isEmpty()) {
            builder.pushStyle(SpanStyle(color = color))
            builder.append(text)
            builder.pop()
            return
        }
        val q = query.lowercase()
        val lower = text.lowercase()
        var i = 0
        while (i < text.length) {
            val match = lower.indexOf(q, i)
            if (match < 0) {
                builder.pushStyle(SpanStyle(color = color))
                builder.append(text.substring(i))
                builder.pop()
                break
            }
            builder.pushStyle(SpanStyle(color = color))
            builder.append(text.substring(i, match))
            builder.pop()
            builder.pushStyle(SpanStyle(background = mark, color = c.fg1))
            builder.append(text.substring(match, match + q.length))
            builder.pop()
            i = match + q.length
        }
    }

    TOKEN_REGEX.findAll(line).forEach { m ->
        if (m.range.first > pos) append(line.substring(pos, m.range.first), c.fg3)
        when {
            m.groups[1] != null -> {
                val isKey = m.groups[2] != null
                val token = m.groups[1]!!.value + (m.groups[2]?.value.orEmpty())
                append(token, if (isKey) colorKey else colorString)
            }
            m.groups[3] != null -> append(m.groups[3]!!.value, colorBool)
            m.groups[4] != null -> append(m.groups[4]!!.value, colorNumber)
            m.groups[5] != null -> append(m.groups[5]!!.value, c.fg3)
        }
        pos = m.range.last + 1
    }
    if (pos < line.length) append(line.substring(pos), c.fg3)
    return builder.toAnnotatedString()
}

private fun prettifyJson(src: String): String {
    val out = StringBuilder()
    var indent = 0
    var inString = false
    var escape = false
    fun newline() {
        out.append('\n')
        repeat(indent) { out.append("  ") }
    }
    for (ch in src) {
        if (escape) { out.append(ch); escape = false; continue }
        if (ch == '\\') { out.append(ch); escape = true; continue }
        if (ch == '"') { inString = !inString; out.append(ch); continue }
        if (inString) { out.append(ch); continue }
        when (ch) {
            '{', '[' -> { out.append(ch); indent++; newline() }
            '}', ']' -> { indent--; newline(); out.append(ch) }
            ',' -> { out.append(ch); newline() }
            ':' -> { out.append(ch); out.append(' ') }
            ' ', '\n', '\r', '\t' -> Unit
            else -> out.append(ch)
        }
    }
    return out.toString()
}
