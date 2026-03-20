package dev.skymansandy.jsonviewer

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.skymansandy.jsonviewer.component.editor.CodeEditor
import dev.skymansandy.jsonviewer.component.editor.EditorToolbar
import dev.skymansandy.jsonviewer.component.editor.ErrorBanner
import dev.skymansandy.jsonviewer.component.viewer.JsonViewer
import dev.skymansandy.jsonviewer.config.JsonEditorState
import dev.skymansandy.jsonviewer.helper.constants.colors.JsonViewerColors
import dev.skymansandy.jsonviewer.helper.parser.JsonError
import dev.skymansandy.jsonviewer.model.JsonNode

@Composable
fun JsonCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    colors: JsonViewerColors = JsonViewerColors.Dark,
    onJsonChange: (
        json: String,
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _, _ -> },
) {

    LaunchedEffect(state.rawJson, state.parsedJson, state.error) {
        onJsonChange(state.rawJson, state.parsedJson, state.error)
    }

    Column(
        modifier = modifier,
    ) {
        if (state.isEditing) {
            EditorToolbar(
                state = state,
                colors = colors,
            )

            ErrorBanner(
                error = state.error,
                colors = colors,
            )

            CodeEditor(
                state = state,
                searchQuery = searchQuery,
                colors = colors,
            )
        } else {
            JsonViewer(
                state = state,
                searchQuery = searchQuery,
                colors = colors,
            )
        }
    }
}
