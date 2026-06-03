/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.skymansandy.wiretap.design.theme.WiretapDesign

enum class MessageDirection { Sent, Received }

@Composable
fun MessageBubble(
    direction: MessageDirection,
    content: String,
    timestamp: String,
    size: String,
    kind: String = "Text",
    modifier: Modifier = Modifier,
) {
    val c = WiretapDesign.colors
    val (bg, border) = when (direction) {
        MessageDirection.Sent -> c.accentSoft to c.accentLine
        MessageDirection.Received -> c.surface1 to c.border1
    }
    val accentColor = when (direction) {
        MessageDirection.Sent -> c.accent
        MessageDirection.Received -> c.methodPost
    }
    val align = if (direction == MessageDirection.Sent) Alignment.End else Alignment.Start

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .align(when (align) { Alignment.End -> Alignment.CenterEnd; else -> Alignment.CenterStart })
                .widthIn(max = 280.dp)
                .background(bg, RoundedCornerShape(10.dp))
                .border(1.dp, border, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (direction == MessageDirection.Sent) "↑ SENT" else "↓ RECEIVED",
                    style = WiretapDesign.typography.label.copy(letterSpacing = 0.08.em),
                    color = accentColor,
                )
                Text(text = "· $kind", style = WiretapDesign.typography.monoMeta, color = c.fg3)
                Text(
                    text = "$timestamp · $size",
                    style = WiretapDesign.typography.monoMeta,
                    color = c.fg4,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(text = content, style = WiretapDesign.typography.code, color = c.fg1)
        }
    }
}
