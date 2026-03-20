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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretapsample.model.WsLogEntry
import dev.skymansandy.wiretapsample.model.WsLogEntry.WsMsgType
import dev.skymansandy.wiretapsample.resources.Res
import dev.skymansandy.wiretapsample.resources.received_indicator
import dev.skymansandy.wiretapsample.resources.sent_indicator
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WsMessageItem(entry: WsLogEntry) {

    val (bgColor, textColor, alignment) = when (entry.type) {
        WsMsgType.Sent -> Triple(
            Color(0xFF7E57C2).copy(alpha = 0.15f),
            Color(0xFF7E57C2),
            Alignment.CenterEnd,
        )
        WsMsgType.Recv -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            Alignment.CenterStart,
        )
        WsMsgType.Sys -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.Center,
        )
    }

    if (entry.type == WsMsgType.Sys) {
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
                    text = if (entry.type == WsMsgType.Sent) stringResource(Res.string.sent_indicator) else stringResource(Res.string.received_indicator),
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

@Preview
@Composable
private fun WsMessageItemSentPreview() {
    MaterialTheme {
        WsMessageItem(entry = WsLogEntry(type = WsMsgType.Sent, text = """{"type":"subscribe","channel":"updates"}"""))
    }
}

@Preview
@Composable
private fun WsMessageItemRecvPreview() {
    MaterialTheme {
        WsMessageItem(entry = WsLogEntry(type = WsMsgType.Recv, text = """{"type":"message","data":{"id":1,"status":"ok"}}"""))
    }
}

@Preview
@Composable
private fun WsMessageItemSysPreview() {
    MaterialTheme {
        WsMessageItem(entry = WsLogEntry(type = WsMsgType.Sys, text = "Connected to wss://echo.websocket.org"))
    }
}
