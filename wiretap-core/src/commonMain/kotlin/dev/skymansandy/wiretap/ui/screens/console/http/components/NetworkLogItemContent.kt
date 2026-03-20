package dev.skymansandy.wiretap.ui.screens.console.http.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.formatOneDecimal
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.ui.common.highlightText
import dev.skymansandy.wiretap.ui.theme.WiretapColors

@Composable
internal fun NetworkLogItemContent(
    modifier: Modifier = Modifier,
    entry: HttpLogEntry,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val statusColor = when {
        entry.isInProgress -> WiretapColors.StatusBlue
        entry.responseCode in 200..299 -> WiretapColors.StatusGreen
        entry.responseCode in 300..399 -> WiretapColors.StatusBlue
        entry.responseCode in 400..499 -> WiretapColors.StatusAmber
        entry.responseCode >= 500 -> WiretapColors.StatusRed
        else -> WiretapColors.StatusGray
    }

    val isHttps = entry.url.startsWith("https://", ignoreCase = true)
    val withoutScheme = entry.url.substringAfter("://")
    val host = withoutScheme.substringBefore("/").substringBefore("?")
    val path = withoutScheme.removePrefix(host).ifEmpty { "/" }

    val responseBytes = entry.responseBody?.encodeToByteArray()?.size ?: 0
    val formattedSize = when {
        responseBytes >= 1_048_576 -> "${formatOneDecimal(responseBytes / 1_048_576f)} MB"
        responseBytes >= 1_024 -> "${formatOneDecimal(responseBytes / 1_024f)} kB"
        responseBytes > 0 -> "$responseBytes B"
        else -> null
    }

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = when {
                    entry.isInProgress -> "..."
                    entry.responseCode > 0 -> entry.responseCode.toString()
                    entry.responseCode == -1 -> "!!!"
                    else -> "ERR"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.width(44.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = highlightText("${entry.method} $path", searchQuery),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (isHttps) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = WiretapColors.SecureHost,
                        )
                    }

                    Text(
                        text = highlightText(host, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        color = WiretapColors.SecureHost,
                    )
                }

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = "${entry.durationMs} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (formattedSize != null) {
                        Text(
                            text = formattedSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (entry.source != ResponseSource.Network) {
                        SourceChip(source = entry.source)
                    }
                }
            }
        }

        HorizontalDivider()
    }
}


@Preview
@Composable
private fun NetworkLogItemSuccessPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = HttpLogEntry(
                id = 1,
                url = "https://api.example.com/users/123?include=profile",
                method = "GET",
                responseCode = 200,
                durationMs = 142,
                timestamp = 1710850000000,
                responseBody = """{"name":"John"}""",
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemErrorPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = HttpLogEntry(
                id = 2,
                url = "https://api.example.com/auth/login",
                method = "POST",
                responseCode = 401,
                durationMs = 89,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemInProgressPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = HttpLogEntry(
                id = 3,
                url = "https://api.example.com/data/sync",
                method = "POST",
                responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemMockedPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = HttpLogEntry(
                id = 4,
                url = "https://api.example.com/users",
                method = "GET",
                responseCode = 200,
                durationMs = 5,
                timestamp = 1710850000000,
                source = ResponseSource.Mock,
                matchedRuleId = 1,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NetworkLogItemServerErrorPreview() {
    MaterialTheme {
        NetworkLogItemContent(
            entry = HttpLogEntry(
                id = 5,
                url = "http://api.example.com/internal/health",
                method = "GET",
                responseCode = 500,
                durationMs = 2034,
                timestamp = 1710850000000,
                responseBody = """{"error":"Internal Server Error"}""",
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

