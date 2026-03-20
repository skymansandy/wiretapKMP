package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.viewer.JsonViewer
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonViewerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysJsonKeys() {
        val state = JsonEditorState("""{"name": "John", "age": 30}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"age\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysJsonValues() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysLineNumbersForMultiLineJson() {
        val state = JsonEditorState(
            """{"a": 1, "b": 2, "c": 3, "d": 4, "e": 5, "f": 6, "g": 7, "h": 8, "i": 9, "j": 10, "k": 11}""",
            isEditing = false,
        )

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("13", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersNestedObjectsWithFoldStructure() {
        val state = JsonEditorState("""{"obj": {"key": "val"}}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"obj\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"key\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsPlainTextFallbackForUnparseableContent() {
        val state = JsonEditorState("not json at all", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("not json at all", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEmptyObjectInline() {
        val state = JsonEditorState("""{"empty": {}}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("{}", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEmptyArrayInline() {
        val state = JsonEditorState("""{"empty": []}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("[]", substring = true).assertIsDisplayed()
    }

    @Test
    fun foldCollapseHidesChildren() {
        val state = JsonEditorState(
            """{"user": {"name": "John", "city": "NYC"}}""",
            isEditing = false,
        )

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        state.collapseAll()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("...", substring = true).assertIsDisplayed()
    }

    @Test
    fun collapseThenExpandShowsChildrenAgain() {
        val state = JsonEditorState(
            """{"name": "John", "age": 30}""",
            isEditing = false,
        )

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        state.collapseAll()
        composeTestRule.waitForIdle()
        state.expandAll()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysBooleanValues() {
        val state = JsonEditorState("""{"flag": true}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("true", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysNullValues() {
        val state = JsonEditorState("""{"val": null}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonViewer(state = state, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("null", substring = true).assertIsDisplayed()
    }
}
