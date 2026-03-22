package dev.skymansandy.wiretap.ui.screens.rule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.ui.model.testRegex
import dev.skymansandy.wiretap.ui.theme.WiretapColors

@Composable
internal fun RegexTesterSheet(
    modifier: Modifier = Modifier,
    pattern: String,
    testInputLabel: String,
    onDismiss: () -> Unit,
) {
    var testInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Regex Tester",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = pattern,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(10.dp),
            )
        }

        OutlinedTextField(
            value = testInput,
            onValueChange = { testInput = it },
            label = { Text(testInputLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (testInput.isNotBlank()) {
            val result = testRegex(pattern, testInput)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.matches) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (result.matches) WiretapColors.StatusGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Column {
                    Text(
                        text = if (result.matches) "Match found" else "No match",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.matches) WiretapColors.StatusGreen else MaterialTheme.colorScheme.error,
                    )
                    if (result.error != null) {
                        Text(
                            text = "Invalid regex: ${result.error}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Preview
@Composable
private fun Preview_RegexTesterSheet() {
    MaterialTheme {
        RegexTesterSheet(
            pattern = "/api/v\\d+/users/.*",
            testInputLabel = "Test URL",
            onDismiss = {},
        )
    }
}
