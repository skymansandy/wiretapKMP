package dev.skymansandy.wiretap.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.ui.screens.console.http.components.tabs.OverviewTab
import org.junit.Rule
import org.junit.Test

class OverviewTabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEntry = HttpLogEntry(
        id = 1,
        url = "https://api.example.com/users/123",
        method = "GET",
        responseCode = 200,
        responseHeaders = mapOf("Content-Type" to "application/json"),
        responseBody = """{"name":"John","age":30}""",
        requestBody = """{"query":"test"}""",
        durationMs = 142,
        timestamp = 1710850000000,
        source = ResponseSource.Network,
        protocol = "HTTP/2",
        remoteAddress = "93.184.216.34:443",
        tlsProtocol = "TLSv1.3",
        cipherSuite = "TLS_AES_256_GCM_SHA384",
    )

    // region Basic fields

    @Test
    fun displaysUrl() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("https://api.example.com/users/123")
            .assertIsDisplayed()
    }

    @Test
    fun displaysMethod() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("GET").assertIsDisplayed()
    }

    @Test
    fun displaysStatusCode() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("200").assertIsDisplayed()
    }

    @Test
    fun displaysDuration() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("142ms").assertIsDisplayed()
    }

    @Test
    fun displaysSource() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("Network").assertIsDisplayed()
    }

    // endregion

    // region TLS info

    @Test
    fun displaysProtocol() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("HTTP/2").assertIsDisplayed()
    }

    @Test
    fun displaysRemoteAddress() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("93.184.216.34:443").assertIsDisplayed()
    }

    @Test
    fun displaysTlsProtocol() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("TLSv1.3").assertIsDisplayed()
    }

    @Test
    fun displaysCipherSuite() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("TLS_AES_256_GCM_SHA384").assertIsDisplayed()
    }

    // endregion

    // region Optional fields hidden when null

    @Test
    fun hidesProtocolWhenNull() {
        val entry = sampleEntry.copy(protocol = null)

        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = entry) }
        }

        composeTestRule.onNodeWithText("HTTP Version").assertDoesNotExist()
    }

    @Test
    fun hidesRemoteAddressWhenNull() {
        val entry = sampleEntry.copy(remoteAddress = null)

        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = entry) }
        }

        composeTestRule.onNodeWithText("Remote Address").assertDoesNotExist()
    }

    @Test
    fun hidesTlsFieldsWhenNull() {
        val entry = sampleEntry.copy(
            tlsProtocol = null,
            cipherSuite = null,
        )

        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = entry) }
        }

        composeTestRule.onNodeWithText("TLS Protocol").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cipher Suite").assertDoesNotExist()
    }

    // endregion

    // region In-progress entry

    @Test
    fun inProgressShowsInProgressStatus() {
        val entry = sampleEntry.copy(
            responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS,
        )

        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = entry) }
        }

        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()
    }

    @Test
    fun inProgressShowsEllipsisForDuration() {
        val entry = sampleEntry.copy(
            responseCode = HttpLogEntry.RESPONSE_CODE_IN_PROGRESS,
        )

        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = entry) }
        }

        composeTestRule.onNodeWithText("...").assertIsDisplayed()
    }

    // endregion

    // region Labels

    @Test
    fun displaysLabels() {
        composeTestRule.setContent {
            MaterialTheme { OverviewTab(entry = sampleEntry) }
        }

        composeTestRule.onNodeWithText("URL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Method").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Duration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Source").assertIsDisplayed()
        composeTestRule.onNodeWithText("Request Size").assertIsDisplayed()
        composeTestRule.onNodeWithText("Response Size").assertIsDisplayed()
    }

    // endregion
}
