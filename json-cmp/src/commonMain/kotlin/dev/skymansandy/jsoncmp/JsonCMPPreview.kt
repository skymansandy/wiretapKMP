package dev.skymansandy.jsoncmp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState

private val sampleJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true,
    "address": {
        "street": "123 Main St",
        "city": "New York",
        "zip": "10001"
    },
    "tags": ["developer", "kotlin", "compose"],
    "score": null
}
""".trimIndent()

@Preview
@Composable
private fun Preview_JsonCMP() {

    MaterialTheme {
        JsonCMP(
            modifier = Modifier.fillMaxSize(),
            state = rememberJsonEditorState(initialJson = sampleJson),
        )
    }
}

@Preview
@Composable
private fun Preview_JsonCMPEditor() {
    MaterialTheme {
        JsonCMP(
            modifier = Modifier.fillMaxSize(),
            state = rememberJsonEditorState(
                initialJson = sampleJson,
                isEditing = true,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_JsonCMPWithSearch() {

    MaterialTheme {
        JsonCMP(
            modifier = Modifier.fillMaxSize(),
            state = rememberJsonEditorState(initialJson = sampleJson),
            searchQuery = "John",
        )
    }
}

@Preview
@Composable
private fun Preview_JsonCMPEmpty() {

    MaterialTheme {
        JsonCMP(
            modifier = Modifier.fillMaxSize(),
            state = rememberJsonEditorState(initialJson = ""),
        )
    }
}

@Preview
@Composable
private fun Preview_JsonCMPInvalidJson() {

    MaterialTheme {
        JsonCMP(
            modifier = Modifier.fillMaxSize(),
            state = rememberJsonEditorState(
                initialJson = """{"name": "John", "age":}""",
                isEditing = true,
            ),
        )
    }
}
