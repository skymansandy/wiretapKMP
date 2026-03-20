package dev.skymansandy.jsoncmp.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditorToolbar(
    state: JsonEditorState,
    colors: JsonCmpColors,
) {
    var showSortSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.gutterBackground)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Format: Beautify / Compact toggle
        IconButton(
            onClick = { state.format(compact = !state.isCompact) },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (state.isCompact) Icons.AutoMirrored.Filled.FormatIndentIncrease else Icons.Default.FormatAlignJustify,
                contentDescription = if (state.isCompact) "Beautify" else "Compact",
                tint = colors.key,
                modifier = Modifier.size(18.dp),
            )
        }

        ToolbarDivider(colors)

        // Sort
        IconButton(
            onClick = { showSortSheet = true },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort keys",
                tint = colors.key,
                modifier = Modifier.size(18.dp),
            )
        }
    }

    if (showSortSheet) {
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            sheetState = sheetState,
            containerColor = colors.gutterBackground,
            contentColor = colors.punctuation,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Sort Keys",
                    style = monoStyle.copy(fontSize = 14.sp),
                    color = colors.key,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                SortOption(
                    label = "Sort Ascending (A \u2192 Z)",
                    colors = colors,
                    onClick = {
                        state.sortKeys(ascending = true)
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showSortSheet = false
                        }
                    },
                )

                HorizontalDivider(color = colors.gutterBorder, thickness = 0.5.dp)

                SortOption(
                    label = "Sort Descending (Z \u2192 A)",
                    colors = colors,
                    onClick = {
                        state.sortKeys(ascending = false)
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showSortSheet = false
                        }
                    },
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Previews ──

@Preview
@Composable
private fun Preview_EditorToolbar() {
    MaterialTheme {
        EditorToolbar(
            state = JsonEditorState(
                initialJson = """{"name": "John"}""",
                isEditing = true,
            ),
            colors = JsonCmpColors.Dark,
        )
    }
}
