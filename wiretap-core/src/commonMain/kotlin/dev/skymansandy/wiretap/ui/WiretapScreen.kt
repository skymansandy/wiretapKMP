package dev.skymansandy.wiretap.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.ui.screens.CreateRuleScreen
import dev.skymansandy.wiretap.ui.screens.NetworkLogDetailScreen
import dev.skymansandy.wiretap.ui.screens.RuleDetailScreen
import dev.skymansandy.wiretap.ui.screens.SocketDetailScreen
import dev.skymansandy.wiretap.ui.screens.WiretapHomeScreen

@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = WiretapDi.orchestrator,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
    findConflictingRules: FindConflictingRulesUseCase = WiretapDi.findConflictingRules,
    initialSocketId: Long? = null,
    onInitialSocketConsumed: () -> Unit = {},
) {
    var route by remember { mutableStateOf<WiretapRoute?>(null) }
    val scope = rememberCoroutineScope()

    // Handle deep-link to socket detail
    LaunchedEffect(initialSocketId) {
        if (initialSocketId != null) {
            route = WiretapRoute.SocketDetail(initialSocketId)
            onInitialSocketConsumed()
        }
    }

    when (val current = route) {
        is WiretapRoute.SocketDetail -> SocketDetailScreen(
            socketId = current.socketId,
            orchestrator = orchestrator,
            onBack = { route = null },
        )

        is WiretapRoute.HttpDetail -> NetworkLogDetailScreen(
            entry = current.entry,
            onBack = { route = null },
            onViewRule = { ruleId ->
                scope.launch {
                    val rule = ruleRepository.getById(ruleId)
                    if (rule != null) route = WiretapRoute.RuleDetail(rule)
                }
            },
        )

        is WiretapRoute.RuleDetail -> RuleDetailScreen(
            rule = current.rule,
            ruleRepository = ruleRepository,
            onBack = { route = null },
            onDeleted = { route = null },
            onEditClick = { route = WiretapRoute.CreateRule(existingRule = current.rule) },
        )

        is WiretapRoute.CreateRule -> CreateRuleScreen(
            ruleRepository = ruleRepository,
            findConflictingRules = findConflictingRules,
            onBack = { route = null },
            onSaved = { route = null },
            existingRule = current.existingRule,
            prefillFromLog = current.prefillFromLog,
            onEditConflictingRule = { rule -> route = WiretapRoute.CreateRule(existingRule = rule) },
        )

        null -> WiretapHomeScreen(
            onBack = onBack,
            orchestrator = orchestrator,
            ruleRepository = ruleRepository,
            onNavigate = { route = it },
        )
    }
}
