package dev.skymansandy.wiretap.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface WiretapRoute : NavKey {

    @Serializable
    data object Home : WiretapRoute

    @Serializable
    data class HttpDetail(val entryId: Long) : WiretapRoute

    @Serializable
    data class SocketDetail(val socketId: Long) : WiretapRoute

    @Serializable
    data class RuleDetail(val ruleId: Long) : WiretapRoute

    @Serializable
    data class CreateRule(
        val existingRuleId: Long = 0L,
        val prefillFromLogId: Long = 0L,
    ) : WiretapRoute
}
