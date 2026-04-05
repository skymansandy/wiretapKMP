/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.http.detail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import dev.skymansandy.wiretap.ui.common.CopyButton
import dev.skymansandy.wiretap.ui.common.HeadersList
import dev.skymansandy.wiretap.ui.common.SectionTitle

@OptIn(ExperimentalJsonCmpApi::class)
@Composable
internal fun RequestTab(
    modifier: Modifier = Modifier,
    entry: HttpLog,
    searchQuery: String = "",
) {
    val body = entry.requestBody
    val isJson = remember(body) {
        body != null && looksLikeJson(body)
    }

    var headersExpanded by remember { mutableStateOf(body.isNullOrBlank()) }

    Column(
        modifier = modifier.then(
            if (isJson) Modifier else Modifier.verticalScroll(rememberScrollState()),
        ),
    ) {
        val headersCopyText = remember(entry.requestHeaders) {
            entry.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        }
        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Headers",
            action = if (entry.requestHeaders.isNotEmpty()) ({
                CopyButton(
                    text = headersCopyText,
                    snackbarMessage = "Request headers copied to clipboard",
                )
            }) else null,
            expanded = headersExpanded,
            onToggleExpand = { headersExpanded = !headersExpanded },
        )

        AnimatedVisibility(visible = headersExpanded) {
            HeadersList(
                headers = entry.requestHeaders,
                emptyText = "No headers",
                searchQuery = searchQuery,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        SectionTitle(
            modifier = Modifier.fillMaxWidth(),
            text = "Body",
            action = if (body != null) ({
                CopyButton(
                    text = body,
                    snackbarMessage = "Request body copied to clipboard",
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
private fun Preview_RequestTab() {
    MaterialTheme {
        RequestTab(
            entry = HttpLog(
                id = 1,
                url = "https://api.example.com/users",
                method = "POST",
                requestHeaders = mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer eyJhbGciOi...",
                    "Accept" to "application/json",
                ),
                requestBody = """{"name":"John","email":"john@example.com"}""",
                responseCode = 201,
                timestamp = 1710850000000,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_RequestTabEmpty() {
    MaterialTheme {
        RequestTab(
            entry = HttpLog(
                id = 2,
                url = "https://api.example.com/users/123",
                method = "GET",
                responseCode = 200,
                timestamp = 1710850000000,
            ),
        )
    }
}
