package dev.skymansandy.jsonviewer.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsonviewer.helper.constants.colors.JsonViewerColors
import dev.skymansandy.jsonviewer.helper.constants.typography.monoStyle

@Composable
internal fun SortOption(
    label: String,
    colors: JsonViewerColors,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = monoStyle.copy(fontSize = 13.sp),
        color = colors.punctuation,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

// ── Previews ──

@Preview
@Composable
private fun Preview_SortOption() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JsonViewerColors.Dark.gutterBackground),
        ) {
            SortOption(
                label = "Sort Ascending (A \u2192 Z)",
                colors = JsonViewerColors.Dark,
                onClick = {},
            )
        }
    }
}
