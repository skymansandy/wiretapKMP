package dev.skymansandy.wiretap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun KeyValueTable(rows: List<Pair<String, String>>) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            rows.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.35f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.65f),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun KeyValueTablePreview() {
    MaterialTheme {
        KeyValueTable(
            rows = listOf(
                "URL" to "https://api.example.com/users/123",
                "Method" to "GET",
                "Status" to "200",
                "Duration" to "142ms",
                "Size" to "2.4 kB",
            ),
        )
    }
}
