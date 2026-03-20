package dev.skymansandy.jsonviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.skymansandy.jsonviewer.component.editor.SortOption
import dev.skymansandy.jsonviewer.helper.constants.colors.JsonViewerColors
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class SortOptionUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonViewerColors.Dark

    @Test
    fun displaysLabelText() {
        composeTestRule.setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.gutterBackground),
                ) {
                    SortOption(
                        label = "Sort Ascending (A \u2192 Z)",
                        colors = colors,
                        onClick = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Sort Ascending (A \u2192 Z)").assertIsDisplayed()
    }

    @Test
    fun clickCallbackFires() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.gutterBackground),
                ) {
                    SortOption(
                        label = "Sort Descending",
                        colors = colors,
                        onClick = { clicked = true },
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Sort Descending").performClick()
        composeTestRule.waitForIdle()

        clicked shouldBe true
    }

    @Test
    fun displaysCustomLabel() {
        composeTestRule.setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.gutterBackground),
                ) {
                    SortOption(
                        label = "Custom Sort Label",
                        colors = colors,
                        onClick = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Custom Sort Label").assertIsDisplayed()
    }
}
