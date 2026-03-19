package dev.skymansandy.wiretapsample.ui.websocket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretapsample.model.WsLogEntry

@Composable
internal fun WsMessageItem(entry: WsLogEntry) {
    val (bgColor, textColor, alignment) = when (entry.direction) {
        "SENT" -> Triple(
            Color(0xFF7E57C2).copy(alpha = 0.15f),
            Color(0xFF7E57C2),
            Alignment.CenterEnd,
        )
        "RECV" -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            Alignment.CenterStart,
        )
        else -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.Center,
        )
    }

    if (entry.direction == "SYS") {
        Text(
            text = entry.text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        )
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            contentAlignment = alignment,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = if (entry.direction == "SENT") "\u2191 " else "\u2193 ",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )
                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                )
            }
        }
    }
}
