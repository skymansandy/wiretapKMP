package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.common.GutterCell
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GutterCellUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    private val nonFoldableLine = JsonLine(
        lineNumber = 5,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("  "),
            JsonPart.Key("\"name\""),
            JsonPart.Punct(": "),
            JsonPart.StrVal("\"John\""),
        ),
        foldId = null,
        foldType = null,
        parentFoldIds = emptyList(),
    )

    private val foldableLine = JsonLine(
        lineNumber = 3,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("  "),
            JsonPart.Key("\"address\""),
            JsonPart.Punct(": "),
            JsonPart.Punct("{"),
        ),
        foldId = 1,
        foldType = FoldType.Object,
        parentFoldIds = emptyList(),
        foldChildCount = 2,
    )

    @Test
    fun displaysLineNumber() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = nonFoldableLine,
                    isFolded = false,
                    numDigits = 2,
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText(" 5").assertIsDisplayed()
    }

    @Test
    fun padsLineNumberToNumDigits() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = nonFoldableLine,
                    isFolded = false,
                    numDigits = 3,
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("  5").assertIsDisplayed()
    }

    @Test
    fun showsExpandedGlyphForFoldableLine() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = foldableLine,
                    isFolded = false,
                    numDigits = 2,
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").assertIsDisplayed()
    }

    @Test
    fun showsCollapsedGlyphWhenFolded() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = foldableLine,
                    isFolded = true,
                    numDigits = 2,
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\u25B6").assertIsDisplayed()
    }

    @Test
    fun noFoldGlyphForNonFoldableLine() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = nonFoldableLine,
                    isFolded = false,
                    numDigits = 2,
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").assertDoesNotExist()
        composeTestRule.onNodeWithText("\u25B6").assertDoesNotExist()
    }

    @Test
    fun foldToggleCallbackFiresOnClick() {
        var toggled = false

        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    line = foldableLine,
                    isFolded = false,
                    numDigits = 2,
                    colors = colors,
                    onFoldToggle = { toggled = true },
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").performClick()
        composeTestRule.waitForIdle()

        toggled shouldBe true
    }
}
