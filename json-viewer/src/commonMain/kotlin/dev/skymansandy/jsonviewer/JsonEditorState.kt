package dev.skymansandy.jsonviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class JsonEditorState(initialJson: String) {
    var rawJson: String by mutableStateOf(initialJson)
        private set

    var parsedJson: JsonNode? by mutableStateOf(null)
        private set

    var error: JsonError? by mutableStateOf(null)
        private set

    var isCompact: Boolean by mutableStateOf(false)
        private set

    init {
        reparse(initialJson)
    }

    fun updateRawJson(newJson: String) {
        rawJson = newJson
        reparse(newJson)
    }

    fun format(compact: Boolean) {
        val node = parsedJson ?: return
        isCompact = compact
        rawJson = node.toJsonString(compact = compact)
    }

    fun sortKeys(ascending: Boolean) {
        val node = parsedJson ?: return
        val sorted = node.sortKeys(ascending = ascending, recursive = true)
        parsedJson = sorted
        error = null
        rawJson = sorted.toJsonString(compact = isCompact)
    }

    private fun reparse(json: String) {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) {
            parsedJson = null
            error = null
            return
        }
        val (node, err) = parseJsonResult(trimmed)
        parsedJson = node
        error = err
    }
}

@Composable
fun rememberJsonEditorState(initialJson: String): JsonEditorState {
    return remember { JsonEditorState(initialJson) }
}
