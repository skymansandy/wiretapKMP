package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.common.ContentCell
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonLine
import dev.skymansandy.jsoncmp.model.JsonPart
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContentCellUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    private val simpleLine = JsonLine(
        lineNumber = 2,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("  "),
            JsonPart.Key("\"name\""),
            JsonPart.Punct(": "),
            JsonPart.StrVal("\"John Doe\""),
            JsonPart.Punct(","),
        ),
        foldId = null,
        foldType = null,
        parentFoldIds = emptyList(),
    )

    private val foldableLine = JsonLine(
        lineNumber = 5,
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
        foldChildCount = 3,
        foldedContent = "\"street\": \"123 Main St\", \"city\": \"NYC\" }",
    )

    private val arrayFoldableLine = JsonLine(
        lineNumber = 3,
        depth = 1,
        parts = listOf(
            JsonPart.Indent("  "),
            JsonPart.Key("\"tags\""),
            JsonPart.Punct(": "),
            JsonPart.Punct("["),
        ),
        foldId = 2,
        foldType = FoldType.Array,
        parentFoldIds = emptyList(),
        foldChildCount = 2,
        foldedContent = "\"a\", \"b\" ]",
    )

    @Test
    fun displaysLineTextWhenNotFolded() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = simpleLine,
                    isFolded = false,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"John Doe\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEllipsisWhenObjectIsFolded() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = foldableLine,
                    isFolded = true,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("...", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("}", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysClosingBracketForFoldedArray() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = arrayFoldableLine,
                    isFolded = true,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("...", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("]", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsFullTextWhenFoldableButNotFolded() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = foldableLine,
                    isFolded = false,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("{", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("...").assertDoesNotExist()
    }

    @Test
    fun rendersWithSearchQueryWithoutCrash() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = simpleLine,
                    isFolded = false,
                    searchQuery = "John",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\"John Doe\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersFoldedWithSearchQueryMatchingFoldedContent() {
        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = foldableLine,
                    isFolded = true,
                    searchQuery = "Main St",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("...", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersNumberValue() {
        val numLine = JsonLine(
            lineNumber = 3,
            depth = 1,
            parts = listOf(
                JsonPart.Indent("  "),
                JsonPart.Key("\"age\""),
                JsonPart.Punct(": "),
                JsonPart.NumVal("30"),
            ),
            foldId = null,
            foldType = null,
            parentFoldIds = emptyList(),
        )

        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = numLine,
                    isFolded = false,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("30", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersBooleanValue() {
        val boolLine = JsonLine(
            lineNumber = 4,
            depth = 1,
            parts = listOf(
                JsonPart.Indent("  "),
                JsonPart.Key("\"active\""),
                JsonPart.Punct(": "),
                JsonPart.BoolVal("true"),
            ),
            foldId = null,
            foldType = null,
            parentFoldIds = emptyList(),
        )

        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = boolLine,
                    isFolded = false,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("true", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersNullValue() {
        val nullLine = JsonLine(
            lineNumber = 5,
            depth = 1,
            parts = listOf(
                JsonPart.Indent("  "),
                JsonPart.Key("\"val\""),
                JsonPart.Punct(": "),
                JsonPart.NullVal("null"),
            ),
            foldId = null,
            foldType = null,
            parentFoldIds = emptyList(),
        )

        composeTestRule.setContent {
            MaterialTheme {
                ContentCell(
                    line = nullLine,
                    isFolded = false,
                    searchQuery = "",
                    colors = colors,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("null", substring = true).assertIsDisplayed()
    }
}
