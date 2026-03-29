package dev.skymansandy.wiretap.ui.screens.http.detail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.ui.viewer.JsonViewerCMP
import dev.skymansandy.jsoncmp.ui.viewer.rememberJsonViewerState
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.helper.util.looksLikeJson
import dev.skymansandy.wiretap.ui.common.CodeBlock
import dev.skymansandy.wiretap.ui.common.CopyBodyButton
import dev.skymansandy.wiretap.ui.common.CopyHeadersButton
import dev.skymansandy.wiretap.ui.common.HeadersList
import dev.skymansandy.wiretap.ui.common.SectionTitle

@OptIn(ExperimentalJsonCmpApi::class)
@Composable
internal fun ResponseTab(
    modifier: Modifier = Modifier,
    entry: HttpLog,
    searchQuery: String = "",
) {
    val body = entry.responseBody
    val isJson = remember(body) {
        body != null && looksLikeJson(body)
    }

    var headersExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.then(
            if (isJson) Modifier else Modifier.verticalScroll(rememberScrollState()),
        ),
    ) {
        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Headers",
            action = if (entry.responseHeaders.isNotEmpty()) ({
                CopyHeadersButton(
                    headers = entry.responseHeaders,
                    snackbarMessage = "Response headers copied to clipboard",
                )
            }) else null,
            expanded = headersExpanded,
            onToggleExpand = { headersExpanded = !headersExpanded },
        )

        AnimatedVisibility(visible = headersExpanded) {
            HeadersList(
                headers = entry.responseHeaders,
                emptyText = "No headers",
                searchQuery = searchQuery,
            )
        }

        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Body",
            action = if (body != null) ({
                CopyBodyButton(
                    body = body,
                    snackbarMessage = "Response body copied to clipboard",
                )
            }) else null,
        )

        if (isJson) {
            val jsonState = rememberJsonViewerState(json = body!!)
            JsonViewerCMP(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                state = jsonState,
                searchQuery = searchQuery,
            )
        } else {
            CodeBlock(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                text = body ?: "No body",
                searchQuery = searchQuery,
            )
        }
    }
}

@Preview
@Composable
private fun Preview_ResponseTab() {
    MaterialTheme {
        ResponseTab(
            entry = HttpLog(
                id = 1,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                responseHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Cache-Control" to "max-age=3600",
                    "X-Request-Id" to "abc-123-def",
                ),
                responseBody = """{"id":123,"name":"John","email":"john@example.com","age":30}""",
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_ResponseTabEmpty() {
    MaterialTheme {
        ResponseTab(
            entry = HttpLog(
                id = 2,
                url = "https://api.example.com/users/123",
                method = "DELETE",
                responseCode = 204,
                timestamp = 1710850000000,
            ),
        )
    }
}
