/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.helper.util.formatSizeOrNull
import dev.skymansandy.wiretap.helper.util.formatTime
import dev.skymansandy.wiretap.helper.util.highlightText
import dev.skymansandy.wiretap.ui.theme.WiretapColors

@Composable
internal fun HttpLogItemContent(
    modifier: Modifier = Modifier,
    entry: HttpLog,
    searchQuery: String,
    onClick: () -> Unit,
) {
    val isHttps = remember(entry) { entry.url.startsWith("https://", ignoreCase = true) }
    val withoutScheme = remember(entry) { entry.url.substringAfter("://") }
    val host = remember(withoutScheme) { withoutScheme.substringBefore("/").substringBefore("?") }
    val path = remember(withoutScheme) { withoutScheme.removePrefix(host).ifEmpty { "/" } }

    val formattedSize = remember(entry) { formatSizeOrNull(entry.responseBodySize) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background),
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
                text = entry.statusText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = entry.statusColor,
                modifier = Modifier.width(44.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = entry.statusColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    text = remember(entry.method, path, searchQuery) {
                        highlightText("${entry.method} $path", searchQuery)
                    },
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        text = remember(host, searchQuery) {
                            highlightText(host, searchQuery)
                        },
                    )

                    if (entry.source != ResponseSource.Network) {
                        SourceChip(source = entry.source)
                    }
                }

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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

                    Text(
                        text = formatTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        HorizontalDivider()
    }
}

@Preview
@Composable
private fun Preview_HttpLogItemSuccess() {
    MaterialTheme {
        HttpLogItemContent(
            entry = HttpLog(
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
private fun Preview_HttpLogItemError() {
    MaterialTheme {
        HttpLogItemContent(
            entry = HttpLog(
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
private fun Preview_HttpLogItemInProgress() {
    MaterialTheme {
        HttpLogItemContent(
            entry = HttpLog(
                id = 3,
                url = "https://api.example.com/data/sync",
                method = "POST",
                responseCode = HttpLog.RESPONSE_CODE_IN_PROGRESS,
                timestamp = 1710850000000,
            ),
            searchQuery = "",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun Preview_HttpLogItemMocked() {
    MaterialTheme {
        HttpLogItemContent(
            entry = HttpLog(
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
private fun Preview_HttpLogItemServerError() {
    MaterialTheme {
        HttpLogItemContent(
            entry = HttpLog(
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
