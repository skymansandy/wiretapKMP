package dev.skymansandy.wiretap.ui.screens.http.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.domain.model.HttpLog

@Composable
internal fun StatusHeader(
    modifier: Modifier = Modifier,
    entry: HttpLog,
) {

    val icon = when {
        entry.isInProgress -> Icons.Default.HourglassEmpty
        entry.responseCode in 200..299 -> Icons.Default.CheckCircle
        entry.responseCode in 300..399 -> Icons.Default.Forward
        entry.responseCode in 400..499 -> Icons.Default.Warning
        entry.responseCode >= 500 -> Icons.Default.Error
        entry.responseCode == -1 -> Icons.Default.Cancel
        else -> Icons.Default.Error
    }

    val statusText = when {
        entry.isInProgress -> "In Progress"
        entry.responseCode == -1 -> "Cancelled"
        entry.responseCode == 0 -> "Network Error"
        else -> "${entry.responseCode} ${httpStatusText(entry.responseCode)}"
    }

    val statusColor = entry.statusColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(20.dp),
        )

        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = statusColor,
            modifier = Modifier.weight(1f),
        )

        if (!entry.isInProgress) {
            Text(
                text = formatDuration(entry.durationMs),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

private fun formatDuration(ms: Long): String = when {
    ms < 1 -> "<1 ms"
    ms < 1000 -> "$ms ms"
    else -> "${formatOneDecimal(ms / 1000.0)} s"
}

private fun formatOneDecimal(value: Double): String {
    val int = value.toLong()
    val frac = ((value - int) * 10).toLong()
    return "$int.$frac"
}

private fun httpStatusText(code: Int): String = when (code) {
    100 -> "Continue"
    101 -> "Switching Protocols"
    200 -> "OK"
    201 -> "Created"
    202 -> "Accepted"
    204 -> "No Content"
    206 -> "Partial Content"
    301 -> "Moved Permanently"
    302 -> "Found"
    304 -> "Not Modified"
    307 -> "Temporary Redirect"
    308 -> "Permanent Redirect"
    400 -> "Bad Request"
    401 -> "Unauthorized"
    403 -> "Forbidden"
    404 -> "Not Found"
    405 -> "Method Not Allowed"
    408 -> "Request Timeout"
    409 -> "Conflict"
    410 -> "Gone"
    413 -> "Payload Too Large"
    415 -> "Unsupported Media Type"
    422 -> "Unprocessable Entity"
    429 -> "Too Many Requests"
    500 -> "Internal Server Error"
    502 -> "Bad Gateway"
    503 -> "Service Unavailable"
    504 -> "Gateway Timeout"
    else -> ""
}
