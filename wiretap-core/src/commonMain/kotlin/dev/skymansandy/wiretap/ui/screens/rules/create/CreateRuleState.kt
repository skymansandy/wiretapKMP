/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.create

import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap.ui.model.ThrottleInputMode
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// -------------------- Prefill config ---------------------------
internal data class PrefillConfig(
    val logId: Long = 0L,
    val includeUrl: Boolean = true,
    val includeHeaders: Boolean = true,
    val includeBody: Boolean = true,
    val selectedHeaderKeys: String = "",
)

// -------------------- Request state ----------------------------
internal data class RequestStepState(
    val method: String = "*",
    val urlMode: UrlMatchMode? = null,
    val urlPattern: String = "",
    val headerEntries: List<HeaderEntry> = emptyList(),
    val bodyMode: BodyMatchMode? = null,
    val bodyPattern: String = "",
)

internal inline fun MutableStateFlow<RequestStepState>.updateRequest(
    crossinline transform: RequestStepState.() -> RequestStepState,
) = update { it.transform() }

// -------------------- Response state ----------------------------
internal data class ResponseStepState(
    val action: RuleAction = RuleAction.Mock(),
    val mockResponseCode: String = "200",
    val mockResponseBody: String = "",
    val responseHeaderEntries: List<ResponseHeaderEntry> = emptyList(),
    val responseHeadersBulk: String = "",
    val responseHeadersMode: ResponseHeadersEditMode = ResponseHeadersEditMode.KeyValue,
    val throttleDelayMs: String = "",
    val throttleDelayMaxMs: String = "",
    val throttleInputMode: ThrottleInputMode = ThrottleInputMode.None,
)

internal inline fun MutableStateFlow<ResponseStepState>.updateResponse(
    crossinline transform: ResponseStepState.() -> ResponseStepState,
) = update { it.transform() }
