package dev.skymansandy.wiretap.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface WiretapScreen : NavKey {

    @Serializable
    data object HomeScreen : WiretapScreen

    @Serializable
    data class HttpDetailScreen(val entryId: Long) : WiretapScreen

    @Serializable
    data class SocketDetailScreen(val socketId: Long) : WiretapScreen

    @Serializable
    data class RuleDetailScreen(val ruleId: Long) : WiretapScreen

    @Serializable
    data class CreateRuleScreen(
        val existingRuleId: Long = 0L,
        val prefillFromLogId: Long = 0L,
    ) : WiretapScreen
}
