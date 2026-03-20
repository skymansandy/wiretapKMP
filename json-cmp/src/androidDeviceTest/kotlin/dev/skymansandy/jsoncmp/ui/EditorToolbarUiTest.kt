package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.editor.EditorToolbar
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.model.JsonNode
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorToolbarUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun showsFormatButtonWithCompactDescriptionWhenNotCompact() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").assertIsDisplayed()
    }

    @Test
    fun showsFormatButtonWithBeautifyDescriptionWhenCompact() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)
        state.format(compact = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    @Test
    fun showsSortButton() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertIsDisplayed()
    }

    @Test
    fun clickingFormatTogglesCompactMode() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        state.isCompact shouldBe false
        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()

        state.isCompact shouldBe true
        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    @Test
    fun clickingFormatTwiceRoundtrips() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()
        state.isCompact shouldBe true

        composeTestRule.onNodeWithContentDescription("Beautify").performClick()
        composeTestRule.waitForIdle()
        state.isCompact shouldBe false
    }

    @Test
    fun clickingSortOpensBottomSheet() {
        val state = JsonEditorState("""{"a": 1}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sort Keys").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort Ascending (A \u2192 Z)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort Descending (Z \u2192 A)").assertIsDisplayed()
    }

    @Test
    fun sortAscendingReordersKeys() {
        val state = JsonEditorState("""{"c": 3, "a": 1, "b": 2}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sort Ascending (A \u2192 Z)").performClick()
        composeTestRule.waitForIdle()

        val obj = state.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun sortDescendingReordersKeys() {
        val state = JsonEditorState("""{"a": 1, "b": 2, "c": 3}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                EditorToolbar(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sort Descending (Z \u2192 A)").performClick()
        composeTestRule.waitForIdle()

        val obj = state.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("c", "b", "a")
    }
}
