package dev.skymansandy.wiretap.ui.screens.rule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.any_method
import dev.skymansandy.wiretap.resources.close
import dev.skymansandy.wiretap.resources.http_method
import dev.skymansandy.wiretap.resources.match_found
import dev.skymansandy.wiretap.resources.no_match
import dev.skymansandy.wiretap.resources.regex_tester
import dev.skymansandy.wiretap.resources.test_regex
import dev.skymansandy.wiretap.ui.model.testRegex
import dev.skymansandy.wiretap.ui.theme.WiretapColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SectionLabel(
    modifier: Modifier = Modifier,
    title: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.width(8.dp))

        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun RegexTesterIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = stringResource(Res.string.test_regex),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
internal fun StepIndicator(
    modifier: Modifier = Modifier,
    currentStep: Int,
    labels: List<String>,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            val step = index + 1
            val isActive = step == currentStep
            val isCompleted = step < currentStep

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isActive || isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = step.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 6.dp),
            )

            if (index < labels.size - 1) {
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            }
        }
    }
}

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
                stringResource(Res.string.regex_tester),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.close))
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
                        text = if (result.matches) stringResource(Res.string.match_found) else stringResource(
                            Res.string.no_match
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.matches) WiretapColors.StatusGreen else MaterialTheme.colorScheme.error,
                    )
                    if (result.error != null) {
                        Text(
                            text = result.error,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MethodSelector(method: String, onMethodChange: (String) -> Unit) {
    val methods = listOf("*", "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = if (method == "*") stringResource(Res.string.any_method) else method,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.http_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            methods.forEach { m ->
                DropdownMenuItem(
                    text = { Text(if (m == "*") stringResource(Res.string.any_method) else m) },
                    onClick = {
                        onMethodChange(m)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun SectionLabelPreview() {
    MaterialTheme {
        SectionLabel(title = "Request Matching")
    }
}

@Preview
@Composable
private fun StepIndicatorStep1Preview() {
    MaterialTheme {
        StepIndicator(
            currentStep = 1,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}

@Preview
@Composable
private fun StepIndicatorStep2Preview() {
    MaterialTheme {
        StepIndicator(
            currentStep = 2,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}

@Preview
@Composable
private fun StepIndicatorStep3Preview() {
    MaterialTheme {
        StepIndicator(
            currentStep = 3,
            labels = listOf("Request", "Response", "Review"),
        )
    }
}

@Preview
@Composable
private fun RegexTesterSheetPreview() {
    MaterialTheme {
        RegexTesterSheet(
            pattern = "/api/v\\d+/users/.*",
            testInputLabel = "Test URL",
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun MethodSelectorPreview() {
    MaterialTheme {
        MethodSelector(method = "GET", onMethodChange = {})
    }
}
