package dev.skymansandy.wiretap.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.ui.screens.console.http.HttpLogDetailScreen
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class HttpLogDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEntry = HttpLogEntry(
        id = 1,
        url = "https://api.example.com/users/123",
        method = "GET",
        requestHeaders = mapOf(
            "Authorization" to "Bearer token",
            "Accept" to "application/json",
        ),
        responseCode = 200,
        responseHeaders = mapOf("Content-Type" to "application/json"),
        responseBody = """{"id":123,"name":"John"}""",
        durationMs = 142,
        timestamp = 1710850000000,
    )

    // region Title and tabs

    @Test
    fun displaysMethodAndUrlInTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("GET https://api.example.com/users/123")
            .assertIsDisplayed()
    }

    @Test
    fun displaysThreeTabs() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request").assertIsDisplayed()
        composeTestRule.onNodeWithText("Response").assertIsDisplayed()
    }

    @Test
    fun overviewTabSelectedByDefault() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Overview").assertIsSelected()
    }

    @Test
    fun clickingRequestTabSwitchesToRequestContent() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Request").performClick()

        composeTestRule.onNodeWithText("Request").assertIsSelected()
    }

    @Test
    fun clickingResponseTabSwitchesToResponseContent() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Response").performClick()

        composeTestRule.onNodeWithText("Response").assertIsSelected()
    }

    // endregion

    // region Overview tab content

    @Test
    fun overviewTabDisplaysUrlAndMethod() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("https://api.example.com/users/123")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("GET").assertIsDisplayed()
    }

    @Test
    fun overviewTabDisplaysStatusCode() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("200").assertIsDisplayed()
    }

    @Test
    fun overviewTabDisplaysDuration() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("142ms").assertIsDisplayed()
    }

    @Test
    fun overviewTabDisplaysSourceNetwork() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Network").assertIsDisplayed()
    }

    // endregion

    // region In-progress entry

    @Test
    fun inProgressEntryShowsInProgressStatus() {
        val inProgressEntry = sampleEntry.copy(
            responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS,
        )

        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = inProgressEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()
    }

    // endregion

    // region Rule match banner

    @Test
    fun mockedEntryShowsMockBanner() {
        val mockedEntry = sampleEntry.copy(
            source = ResponseSource.Mock,
            matchedRuleId = 1,
        )

        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = mockedEntry,
                    onBack = {},
                    onViewRule = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Mocked by rule").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Rule →").assertIsDisplayed()
    }

    @Test
    fun throttledEntryShowsThrottleBanner() {
        val throttledEntry = sampleEntry.copy(
            source = ResponseSource.Throttle,
            matchedRuleId = 2,
        )

        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = throttledEntry,
                    onBack = {},
                    onViewRule = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Throttled by rule").assertIsDisplayed()
    }

    @Test
    fun networkEntryDoesNotShowBanner() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Mocked by rule").assertDoesNotExist()
        composeTestRule.onNodeWithText("Throttled by rule").assertDoesNotExist()
    }

    @Test
    fun viewRuleBannerClickCallsOnViewRule() {
        var clickedRuleId: Long? = null
        val mockedEntry = sampleEntry.copy(
            source = ResponseSource.Mock,
            matchedRuleId = 42,
        )

        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = mockedEntry,
                    onBack = {},
                    onViewRule = { clickedRuleId = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Mocked by rule").performClick()
        assertEquals(42L, clickedRuleId)
    }

    // endregion

    // region Navigation

    @Test
    fun backButtonCallsOnBack() {
        var backCalled = false

        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = { backCalled = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backCalled)
    }

    // endregion

    // region Search

    @Test
    fun searchIconNotShownOnOverviewTab() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Search").assertDoesNotExist()
    }

    @Test
    fun searchIconShownOnRequestTab() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Request").performClick()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun searchIconShownOnResponseTab() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Response").performClick()
        composeTestRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    // endregion

    // region Share

    @Test
    fun shareButtonIsDisplayed() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }

    @Test
    fun shareMenuShowsOptions() {
        composeTestRule.setContent {
            MaterialTheme {
                HttpLogDetailScreen(
                    entry = sampleEntry,
                    onBack = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Share").performClick()
        composeTestRule.onNodeWithText("Share as text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Share as cURL").assertIsDisplayed()
    }

    // endregion
}
