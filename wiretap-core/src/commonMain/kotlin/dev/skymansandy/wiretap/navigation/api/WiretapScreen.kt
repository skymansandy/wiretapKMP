/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.navigation.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface WiretapScreen : NavKey {

    /** Marker for screens that appear as a fullscreen */
    interface FullScreenPane

    /** Marker for screens that appear in the list pane. */
    interface ListPane

    /** Marker for screens that appear in the detail pane. */
    interface DetailPane

    /** Marker for screens that appear as a bottom sheet overlay. */
    interface BottomSheetPane

    @Serializable
    data object HomeScreen : WiretapScreen, ListPane

    @Serializable
    data class HttpDetailScreen(val entryId: Long) : WiretapScreen, DetailPane

    @Serializable
    data class SocketDetailScreen(val socketId: Long) : WiretapScreen, DetailPane

    @Serializable
    data class RuleDetailScreen(val ruleId: Long) : WiretapScreen, DetailPane

    @Serializable
    data class CreateRuleScreen(
        val existingRuleId: Long = 0L,
        val prefillFromLogId: Long = 0L,
        val includeUrl: Boolean = true,
        val includeHeaders: Boolean = true,
        val includeBody: Boolean = true,
        val selectedHeaderKeys: String = "",
    ) : WiretapScreen, DetailPane

    @Serializable
    data class SelectRuleCriteriaSheet(val logId: Long) : WiretapScreen, BottomSheetPane
}
