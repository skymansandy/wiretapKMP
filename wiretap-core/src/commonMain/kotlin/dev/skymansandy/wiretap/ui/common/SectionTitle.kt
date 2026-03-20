package dev.skymansandy.wiretap.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SectionTitle(
    modifier: Modifier = Modifier,
    text: String,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )

        action?.invoke()
    }
}

@Preview
@Composable
private fun SectionTitlePreview() {
    MaterialTheme {
        SectionTitle(text = "Request Headers")
    }
}

@Preview
@Composable
private fun SectionTitleWithActionPreview() {
    MaterialTheme {
        SectionTitle(
            text = "Response Body",
            action = {
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelSmall,
                )
            },
        )
    }
}
