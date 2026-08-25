/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.sse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.helper.util.formatBytes
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.ui.common.CopyIconButton
import dev.skymansandy.wiretap.ui.screens.sse.detail.SseMatchField

@Composable
internal fun SseEventBubble(
    modifier: Modifier = Modifier,
    event: SseEvent,
    searchQuery: String = "",
    activeMatchField: SseMatchField? = null,
    activeMatchRange: IntRange? = null,
) {
    fun activeRangeFor(field: SseMatchField) = activeMatchRange?.takeIf { activeMatchField == field }

    val bgColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            if (event.eventType != null) {
                Text(
                    text = highlightText(
                        event.eventType,
                        searchQuery,
                        activeRangeFor(SseMatchField.EventType),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(2.dp))
            }

            SelectionContainer {
                Text(
                    text = highlightText(
                        event.data,
                        searchQuery,
                        activeRangeFor(SseMatchField.Data),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                )
            }

            Spacer(Modifier.height(2.dp))

            // An event id is server-supplied and unbounded -- Wikimedia puts a
            // whole JSON array in it -- so it gets its own line, capped at one
            // line. Sharing a row with the metadata, it took the entire width
            // and squeezed the timestamp and size to one character per line.
            event.eventId?.let { id ->
                Text(
                    // "id: " is a label, not part of the value, so the
                    // highlight offsets stay relative to the id itself.
                    text = buildAnnotatedString {
                        append("id: ")
                        append(
                            highlightText(
                                id,
                                searchQuery,
                                activeRangeFor(SseMatchField.EventId),
                            ),
                        )
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End),
                )

                Spacer(Modifier.height(2.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = formatTime(event.timestamp),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )

                Text(
                    text = formatBytes(event.byteCount),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )

                CopyIconButton(
                    text = event.data,
                    contentDescription = "Copy event data",
                    tint = textColor.copy(alpha = 0.6f),
                    snackbarMessage = "Event copied",
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview_SseEventBubble() {
    MaterialTheme {
        SseEventBubble(
            event = SseEvent(
                id = 1,
                connectionId = 1,
                eventType = "message",
                data = """{"type":"edit","title":"Winnebago"}""",
                eventId = "142817583",
                byteCount = 38,
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_SseEventBubbleLongEventId() {
    // Wikimedia's recentchange stream puts a whole JSON array in the id; it has
    // to stay on one line so the timestamp, size and copy button keep their room.
    MaterialTheme {
        SseEventBubble(
            event = SseEvent(
                id = 2,
                connectionId = 1,
                data = """{"${'$'}schema":"/mediawiki/recentchange/1.0.0"}""",
                eventId = "[{\"topic\":\"eqiad.mediawiki.recentchange\",\"partition\":0," +
                    "\"timestamp\":1787598711080},{\"topic\":\"codfw.mediawiki.recentchange\"," +
                    "\"partition\":0,\"offset\":-1}]",
                byteCount = 1024,
                timestamp = 1710850001000,
            ),
        )
    }
}
