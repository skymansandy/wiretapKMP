package dev.skymansandy.jsonviewer.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsonviewer.helper.constants.colors.JsonViewerColors

@Composable
internal fun ToolbarDivider(
    colors: JsonViewerColors,
) {
    Spacer(modifier = Modifier.width(4.dp))

    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(colors.gutterBorder),
    )

    Spacer(modifier = Modifier.width(4.dp))
}

// ── Previews ──

@Preview
@Composable
private fun Preview_ToolbarDivider() {
    MaterialTheme {
        Row(
            modifier = Modifier.background(JsonViewerColors.Dark.gutterBackground),
        ) {
            ToolbarDivider(colors = JsonViewerColors.Dark)
        }
    }
}
