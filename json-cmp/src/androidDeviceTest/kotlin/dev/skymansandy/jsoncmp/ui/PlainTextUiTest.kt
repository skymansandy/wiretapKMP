package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.common.PlainText
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlainTextUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysPlainTextContent() {
        composeTestRule.setContent {
            MaterialTheme {
                PlainText(
                    text = "Hello, this is plain text",
                    searchQuery = "",
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("Hello, this is plain text").assertIsDisplayed()
    }

    @Test
    fun displaysTextWithSearchQuery() {
        composeTestRule.setContent {
            MaterialTheme {
                PlainText(
                    text = "Some content to search in",
                    searchQuery = "content",
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("Some content to search in").assertIsDisplayed()
    }

    @Test
    fun displaysEmptyTextWithoutCrashing() {
        composeTestRule.setContent {
            MaterialTheme {
                PlainText(
                    text = "",
                    searchQuery = "",
                    colors = colors,
                )
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun displaysMultilineText() {
        val multiline = "Line 1\nLine 2\nLine 3"

        composeTestRule.setContent {
            MaterialTheme {
                PlainText(
                    text = multiline,
                    searchQuery = "",
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("Line 1", substring = true).assertIsDisplayed()
    }
}
