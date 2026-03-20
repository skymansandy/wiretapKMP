package dev.skymansandy.wiretap.ui.screens

import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule

internal sealed interface WiretapRoute {

    data class SocketDetail(val socketId: Long) : WiretapRoute

    data class HttpDetail(val entry: HttpLogEntry) : WiretapRoute

    data class RuleDetail(val rule: WiretapRule) : WiretapRoute

    data class CreateRule(
        val existingRule: WiretapRule? = null,
        val prefillFromLog: HttpLogEntry? = null,
    ) : WiretapRoute
}