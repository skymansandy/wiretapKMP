package dev.skymansandy.wiretap.ui.screens.rule.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.any_method
import dev.skymansandy.wiretap.resources.http_method
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MethodSelector(
    modifier: Modifier = Modifier,
    method: String,
    onMethodChange: (String) -> Unit,
) {
    val methods = listOf("*", "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
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
private fun Preview_MethodSelector() {
    MaterialTheme {
        MethodSelector(method = "GET", onMethodChange = {})
    }
}
