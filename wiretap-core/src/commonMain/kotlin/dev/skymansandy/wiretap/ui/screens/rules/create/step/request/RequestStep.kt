/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.create.step.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.ui.model.BODY_SECTION_INFO
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HEADERS_SECTION_INFO
import dev.skymansandy.wiretap.ui.model.URL_SECTION_INFO
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import dev.skymansandy.wiretap.ui.model.bodyFieldLabel
import dev.skymansandy.wiretap.ui.model.bodyPlaceholder
import dev.skymansandy.wiretap.ui.model.bodyWarning
import dev.skymansandy.wiretap.ui.model.headersWarning
import dev.skymansandy.wiretap.ui.model.isRegex
import dev.skymansandy.wiretap.ui.model.urlFieldLabel
import dev.skymansandy.wiretap.ui.model.urlPlaceholder
import dev.skymansandy.wiretap.ui.model.urlWarning
import dev.skymansandy.wiretap.ui.screens.rules.components.ExpandableSection
import dev.skymansandy.wiretap.ui.screens.rules.components.MethodSelector
import dev.skymansandy.wiretap.ui.screens.rules.components.RegexTesterIcon
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RequestStep(
    modifier: Modifier = Modifier,
    viewModel: CreateRuleViewModel,
) {
    val state by viewModel.requestState.collectAsStateWithLifecycle()
    val method = state.method
    val urlMode = state.urlMode
    val urlPattern = state.urlPattern
    val headerEntries = state.headerEntries
    val bodyMode = state.bodyMode
    val bodyPattern = state.bodyPattern

    val testUrlLabel = "Test URL"
    val testHeaderValueLabel = "Test Header Value"
    val testBodyLabel = "Test Body"

    Column(
        modifier = modifier,
    ) {
        // HTTP Method — always visible at the top
        MethodSelector(
            method = method,
            onMethodChange = { viewModel.updateMethod(it) },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        // ── URL ──────────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match URL",
            subtitle = when (urlMode) {
                null -> null
                else -> "${urlMode.label}: ${urlPattern.ifEmpty { "..." }}"
            },
            warning = urlWarning(urlMode, urlPattern),
            infoText = URL_SECTION_INFO,
            initiallyExpanded = true,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = urlMode == null,
                    onClick = { viewModel.updateUrlMode(null) },
                    label = { Text("None") },
                )

                UrlMatchMode.entries.forEach { mode ->
                    FilterChip(
                        selected = urlMode == mode,
                        onClick = { viewModel.updateUrlMode(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }

            if (urlMode != null) {
                OutlinedTextField(
                    value = urlPattern,
                    onValueChange = { viewModel.updateUrlPattern(it) },
                    label = { Text(urlFieldLabel(urlMode)) },
                    placeholder = { Text(urlPlaceholder(urlMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = when {
                        urlMode.isRegex() -> {
                            { RegexTesterIcon { viewModel.openRegexTester(urlPattern, testUrlLabel) } }
                        }

                        else -> null
                    },
                )
            }
        }

        // ── Headers ───────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match Headers",
            subtitle = when {
                headerEntries.isNotEmpty() -> "${headerEntries.size} condition${if (headerEntries.size != 1) "s" else ""}"
                else -> null
            },
            warning = headersWarning(headerEntries),
            infoText = HEADERS_SECTION_INFO,
            initiallyExpanded = headerEntries.isNotEmpty(),
        ) {
            headerEntries.forEachIndexed { idx, entry ->
                HeaderMatcherItem(
                    entry = entry,
                    onUpdate = { viewModel.updateHeader(idx, it) },
                    onRemove = { viewModel.removeHeader(idx) },
                    onOpenRegexTester = {
                        viewModel.openRegexTester(it, testHeaderValueLabel)
                    },
                )
            }

            TextButton(
                onClick = { viewModel.addHeader() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "+ Add Header Condition",
                )
            }
        }

        // ── Body ──────────────────────────────────────────────────────────────────
        ExpandableSection(
            title = "Match Body",
            subtitle = when (bodyMode) {
                null -> null
                else -> bodyMode.label
            },
            warning = bodyWarning(bodyMode, bodyPattern),
            infoText = BODY_SECTION_INFO,
            initiallyExpanded = bodyMode != null,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = bodyMode == null,
                    onClick = { viewModel.updateBodyMode(null) },
                    label = { Text("None") },
                )

                BodyMatchMode.entries.forEach { mode ->
                    FilterChip(
                        selected = bodyMode == mode,
                        onClick = { viewModel.updateBodyMode(mode) },
                        label = { Text(mode.label) },
                    )
                }
            }

            if (bodyMode != null) {
                OutlinedTextField(
                    value = bodyPattern,
                    onValueChange = { viewModel.updateBodyPattern(it) },
                    label = { Text(bodyFieldLabel(bodyMode)) },
                    placeholder = { Text(bodyPlaceholder(bodyMode)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    trailingIcon = when {
                        bodyMode.isRegex() -> {
                            { RegexTesterIcon { viewModel.openRegexTester(bodyPattern, testBodyLabel) } }
                        }

                        else -> null
                    },
                )
            }
        }
    }
}
