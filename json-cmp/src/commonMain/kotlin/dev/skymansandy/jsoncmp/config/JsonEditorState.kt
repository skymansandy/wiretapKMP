package dev.skymansandy.jsoncmp.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.skymansandy.jsoncmp.helper.lines.buildDisplayLines
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import dev.skymansandy.jsoncmp.helper.parser.parseJsonResult
import dev.skymansandy.jsoncmp.helper.serializer.sortKeys
import dev.skymansandy.jsoncmp.helper.serializer.toJsonString
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonNode

@Stable
class JsonEditorState(initialJson: String, isEditing: Boolean) {

    var rawJson: String by mutableStateOf(initialJson)
        private set

    var parsedJson: JsonNode? by mutableStateOf(null)
        private set

    var error: JsonError? by mutableStateOf(null)
        private set

    var isCompact: Boolean by mutableStateOf(false)
        private set

    var isEditing: Boolean by mutableStateOf(isEditing)

    internal val foldState: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    internal var allLines: List<JsonLine> by mutableStateOf(emptyList())
        private set

    init {
        reparse(initialJson)
    }

    fun collapseAll() {

        allLines.forEach { line ->
            line.foldId?.let { foldState[it] = true }
        }
    }

    fun expandAll() {

        foldState.clear()
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
            allLines = emptyList()
            return
        }
        val (node, err) = parseJsonResult(trimmed)
        parsedJson = node
        error = err
        if (node != null) {
            allLines = buildDisplayLines(node)
            val validIds = allLines.mapNotNull { it.foldId }.toSet()
            foldState.keys.removeAll { it !in validIds }
        } else {
            allLines = emptyList()
        }
    }
}

@Composable
fun rememberJsonEditorState(
    initialJson: String,
    isEditing: Boolean = false,
): JsonEditorState {

    return remember {
        JsonEditorState(
            initialJson = initialJson,
            isEditing = isEditing,
        )
    }
}
