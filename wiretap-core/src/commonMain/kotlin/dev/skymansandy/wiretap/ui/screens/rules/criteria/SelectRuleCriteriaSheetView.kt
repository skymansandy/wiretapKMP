/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.criteria

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun SelectRuleCriteriaSheetView(
    logId: Long,
    viewModel: SelectRuleCriteriaViewModel = koinViewModel { parametersOf(logId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val httpLog = state.httpLog ?: return
    val navigator = LocalWiretapNavigator.current
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Match criteria") },
            text = {
                Text(
                    "Choose which parts of this request should be used to match " +
                        "future requests against this rule. Only selected criteria " +
                        "will be compared — unselected fields are ignored.",
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select what to include in rule…",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = { showInfoDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = "${httpLog.method} ${httpLog.url}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // URL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleUrl() },
        ) {
            Checkbox(checked = state.includeUrl, onCheckedChange = { viewModel.toggleUrl() })
            Column {
                Text(text = "URL", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = httpLog.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Headers
        if (httpLog.requestHeaders.isNotEmpty()) {
            val headerToggleState = when {
                state.allHeadersSelected -> ToggleableState.On
                state.includeHeaders -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleAllHeaders() },
            ) {
                TriStateCheckbox(
                    state = headerToggleState,
                    onClick = { viewModel.toggleAllHeaders() },
                )
                Text(
                    text = "Headers (${state.selectedHeaderKeys.size}/${httpLog.requestHeaders.size})",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            AnimatedVisibility(visible = state.includeHeaders) {
                Column(
                    modifier = Modifier.padding(start = 48.dp),
                ) {
                    httpLog.requestHeaders.forEach { (key, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleHeaderKey(key) },
                        ) {
                            Checkbox(
                                checked = key in state.selectedHeaderKeys,
                                onCheckedChange = { viewModel.toggleHeaderKey(key) },
                            )
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }

        // Body
        if (!httpLog.requestBody.isNullOrEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleBody() },
            ) {
                Checkbox(
                    checked = state.includeBody,
                    onCheckedChange = { viewModel.toggleBody() },
                )
                Column {
                    Text(text = "Body", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = httpLog.requestBody.take(100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Continue button
        val hasAnyCriteria = state.includeUrl || state.includeHeaders || state.includeBody
        Button(
            onClick = {
                navigator.pop()
                navigator.pushDetailPane(
                    WiretapScreen.CreateRuleScreen(
                        prefillFromLogId = logId,
                        includeUrl = state.includeUrl,
                        includeHeaders = state.includeHeaders,
                        includeBody = state.includeBody,
                        selectedHeaderKeys = state.selectedHeaderKeys.joinToString("|"),
                    ),
                )
            },
            enabled = hasAnyCriteria,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text("Continue")
        }
    }
}
