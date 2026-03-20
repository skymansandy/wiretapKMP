package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.editor.ErrorBanner
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.parser.JsonError
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorBannerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysErrorMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                ErrorBanner(
                    error = JsonError("Unexpected token at position 5", position = 5),
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("Unexpected token at position 5").assertIsDisplayed()
    }

    @Test
    fun displaysWarningIcon() {
        composeTestRule.setContent {
            MaterialTheme {
                ErrorBanner(
                    error = JsonError("Some error"),
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("\u26A0").assertIsDisplayed()
    }

    @Test
    fun hiddenWhenErrorIsNull() {
        composeTestRule.setContent {
            MaterialTheme {
                ErrorBanner(
                    error = null,
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("\u26A0").assertDoesNotExist()
    }

    @Test
    fun displaysLongErrorMessage() {
        val longMessage = "This is a very long error message that might get truncated in the UI display"

        composeTestRule.setContent {
            MaterialTheme {
                ErrorBanner(
                    error = JsonError(longMessage),
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText(longMessage, substring = true).assertIsDisplayed()
    }
}
