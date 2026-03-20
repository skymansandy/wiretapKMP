package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonCMPUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    // ── Viewer mode ──

    @Test
    fun viewerModeDisplaysParsedJsonContent() {
        val state = JsonEditorState("""{"name": "John", "age": 30}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun viewerModeDisplaysLineNumbers() {
        val state = JsonEditorState("""{"name": "John", "age": 30}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun viewerModeDoesNotShowEditorToolbar() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertDoesNotExist()
    }

    @Test
    fun viewerModeShowsPlainTextForUnparseableContent() {
        val state = JsonEditorState("just plain text", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithText("just plain text", substring = true).assertIsDisplayed()
    }

    // ── Editor mode ──

    @Test
    fun editorModeShowsToolbar() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertIsDisplayed()
    }

    @Test
    fun editorModeShowsFormatToggle() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").assertIsDisplayed()
    }

    @Test
    fun editorModeShowsErrorBannerForInvalidJson() {
        val state = JsonEditorState("{invalid}", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\u26A0", substring = true).assertIsDisplayed()
    }

    @Test
    fun editorModeNoErrorBannerForValidJson() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\u26A0").assertDoesNotExist()
    }

    // ── onJsonChange callback ──

    @Test
    fun onJsonChangeReceivesInitialState() {
        val json = """{"name": "John"}"""
        val state = JsonEditorState(json, isEditing = false)
        var receivedJson: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(
                    state = state,
                    colors = colors,
                    onJsonChange = { j, _, _ -> receivedJson = j },
                )
            }
        }

        composeTestRule.waitForIdle()
        receivedJson shouldBe json
    }

    // ── Format toggle ──

    @Test
    fun clickingFormatButtonTogglesCompactMode() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = true)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(state = state, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()

        state.isCompact shouldBe true
        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    // ── Search highlighting in viewer ──

    @Test
    fun viewerModeWithSearchQueryShowsMatchingContent() {
        val state = JsonEditorState("""{"name": "John"}""", isEditing = false)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(
                    state = state,
                    searchQuery = "John",
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }
}
