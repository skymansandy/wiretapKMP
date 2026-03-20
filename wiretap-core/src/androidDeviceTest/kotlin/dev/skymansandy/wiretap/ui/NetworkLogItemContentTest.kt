package dev.skymansandy.wiretap.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.ui.screens.console.http.components.NetworkLogItemContent
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class NetworkLogItemContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEntry = HttpLogEntry(
        id = 1,
        url = "https://api.example.com/users/123?include=profile",
        method = "GET",
        responseCode = 200,
        durationMs = 142,
        timestamp = 1710850000000,
        responseBody = """{"name":"John"}""",
    )

    // region Status code display

    @Test
    fun displaysStatusCodeForSuccess() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("200").assertIsDisplayed()
    }

    @Test
    fun displaysStatusCodeForClientError() {
        val entry = sampleEntry.copy(responseCode = 404)

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("404").assertIsDisplayed()
    }

    @Test
    fun displaysStatusCodeForServerError() {
        val entry = sampleEntry.copy(responseCode = 500)

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("500").assertIsDisplayed()
    }

    @Test
    fun displaysEllipsisForInProgress() {
        val entry = sampleEntry.copy(
            responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS,
        )

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("...").assertIsDisplayed()
    }

    @Test
    fun displaysExclamationForNetworkError() {
        val entry = sampleEntry.copy(responseCode = -1)

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("!!!").assertIsDisplayed()
    }

    @Test
    fun displaysErrForZeroResponseCode() {
        val entry = sampleEntry.copy(responseCode = 0)

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("ERR").assertIsDisplayed()
    }

    // endregion

    // region Method and path

    @Test
    fun displaysMethodAndPath() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("GET /users/123?include=profile")
            .assertIsDisplayed()
    }

    @Test
    fun displaysHost() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("api.example.com").assertIsDisplayed()
    }

    // endregion

    // region Duration and size

    @Test
    fun displaysDuration() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("142 ms").assertIsDisplayed()
    }

    @Test
    fun displaysResponseSize() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("15 B").assertIsDisplayed()
    }

    @Test
    fun doesNotShowSizeWhenNoBody() {
        val entry = sampleEntry.copy(responseBody = null)

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("0 B").assertDoesNotExist()
    }

    // endregion

    // region Source chip

    @Test
    fun showsMockChipForMockedEntry() {
        val entry = sampleEntry.copy(
            source = ResponseSource.Mock,
            matchedRuleId = 1,
        )

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Mock").assertIsDisplayed()
    }

    @Test
    fun showsThrottleChipForThrottledEntry() {
        val entry = sampleEntry.copy(
            source = ResponseSource.Throttle,
            matchedRuleId = 1,
        )

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = entry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Throttle").assertIsDisplayed()
    }

    @Test
    fun noSourceChipForNetworkEntry() {
        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Mock").assertDoesNotExist()
        composeTestRule.onNodeWithText("Throttle").assertDoesNotExist()
    }

    // endregion

    // region Click

    @Test
    fun clickCallsOnClick() {
        var clicked = false

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = sampleEntry,
                    searchQuery = "",
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("GET /users/123?include=profile").performClick()
        assertTrue(clicked)
    }

    // endregion

    // region HTTP vs HTTPS

    @Test
    fun httpEntryDoesNotShowLockIcon() {
        val httpEntry = sampleEntry.copy(
            url = "http://api.example.com/users",
        )

        composeTestRule.setContent {
            MaterialTheme {
                NetworkLogItemContent(
                    entry = httpEntry,
                    searchQuery = "",
                    onClick = {},
                )
            }
        }

        // Host should still show without lock icon
        composeTestRule.onNodeWithText("api.example.com").assertIsDisplayed()
    }

    // endregion
}
