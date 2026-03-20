package dev.skymansandy.jsonviewer.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsonviewer.helper.constants.colors.JsonViewerColors
import dev.skymansandy.jsonviewer.helper.constants.typography.monoStyle
import dev.skymansandy.jsonviewer.helper.parser.JsonError

@Composable
internal fun ErrorBanner(
    error: JsonError?,
    colors: JsonViewerColors,
) {
    if (error == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = "\u26A0",
            style = monoStyle,
            color = colors.errorForeground,
            modifier = Modifier.padding(end = 8.dp),
        )

        Text(
            text = error.message,
            style = monoStyle.copy(fontSize = 11.sp),
            color = colors.errorForeground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Previews ──

@Preview
@Composable
private fun Preview_ErrorBanner() {
    MaterialTheme {
        ErrorBanner(
            error = JsonError(
                message = "Unexpected token '}' at position 23",
                position = 23,
            ),
            colors = JsonViewerColors.Dark,
        )
    }
}

@Preview
@Composable
private fun Preview_ErrorBannerNull() {
    MaterialTheme {
        ErrorBanner(
            error = null,
            colors = JsonViewerColors.Dark,
        )
    }
}
