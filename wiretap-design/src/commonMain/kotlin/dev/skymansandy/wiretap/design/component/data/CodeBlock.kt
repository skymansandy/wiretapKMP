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

@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
) {
    val c = WiretapDesign.colors
    val lines = remember(code) { code.split('\n') }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.surface0)
            .padding(vertical = 10.dp),
    ) {
        Column {
            lines.forEachIndexed { index, line ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text(
                        text = (index + 1).toString(),
                        style = WiretapDesign.typography.monoMeta,
                        color = c.fg4,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(28.dp).padding(end = 12.dp),
                    )
                    Text(
                        text = highlightMatches(line, searchQuery, c.fg1, c.accentSoft),
                        style = WiretapDesign.typography.code,
                        color = c.fg1,
                    )
                }
            }
        }
    }
}

internal fun highlightMatches(
    text: String,
    query: String,
    fg: Color,
    highlight: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    if (query.isEmpty()) {
        builder.append(text)
        return builder.toAnnotatedString()
    }
    val q = query.lowercase()
    val lower = text.lowercase()
    var i = 0
    while (i < text.length) {
        val match = lower.indexOf(q, i)
        if (match < 0) { builder.append(text.substring(i)); break }
        builder.append(text.substring(i, match))
        builder.pushStyle(SpanStyle(background = highlight, color = fg))
        builder.append(text.substring(match, match + q.length))
        builder.pop()
        i = match + q.length
    }
    return builder.toAnnotatedString()
}
