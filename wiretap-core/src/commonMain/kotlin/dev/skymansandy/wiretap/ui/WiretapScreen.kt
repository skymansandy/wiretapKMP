package dev.skymansandy.wiretap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.screens.WiretapRoute
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeScreen
import dev.skymansandy.wiretap.ui.screens.console.WiretapHomeViewModel
import dev.skymansandy.wiretap.ui.screens.console.http.HttpLogDetailScreen
import dev.skymansandy.wiretap.ui.screens.console.socket.SocketDetailScreen
import dev.skymansandy.wiretap.ui.screens.console.socket.SocketDetailViewModel
import dev.skymansandy.wiretap.ui.screens.rule.CreateRuleScreen
import dev.skymansandy.wiretap.ui.screens.rule.CreateRuleViewModel
import dev.skymansandy.wiretap.ui.screens.rule.RuleDetailScreen
import dev.skymansandy.wiretap.ui.screens.rule.RuleDetailViewModel
import kotlinx.coroutines.launch

private val WIDE_SCREEN_BREAKPOINT = 600.dp

@Composable
@Suppress("CyclomaticComplexMethod")
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = WiretapDi.orchestrator,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
    findConflictingRules: FindConflictingRulesUseCase = WiretapDi.findConflictingRules,
    initialSocketId: Long? = null,
    onInitialSocketConsumed: () -> Unit = {},
) {
    var savedRouteKey by rememberSaveable { mutableStateOf<String?>(null) }
    var route by remember { mutableStateOf<WiretapRoute?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var isWideScreen by remember { mutableStateOf(false) }

    val navigateTo = remember(orchestrator, ruleRepository) {
        { newRoute: WiretapRoute? ->
            route = newRoute
            savedRouteKey = newRoute?.toSaveKey()
        }
    }

    // Restore route after config change (e.g. rotation)
    LaunchedEffect(Unit) {
        val key = savedRouteKey ?: return@LaunchedEffect
        if (route != null) return@LaunchedEffect

        val restored = restoreRoute(key, orchestrator, ruleRepository)
        if (restored != null) route = restored
    }

    // Handle deep-link to socket detail
    LaunchedEffect(initialSocketId) {
        if (initialSocketId != null) {
            navigateTo(WiretapRoute.SocketDetail(initialSocketId))
            onInitialSocketConsumed()
        }
    }

    CompositionLocalProvider(LocalWideScreen provides isWideScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    isWideScreen = with(density) { size.width.toDp() } >= WIDE_SCREEN_BREAKPOINT
                },
        ) {

            val homeVm = viewModel { WiretapHomeViewModel(orchestrator, ruleRepository) }

            // Sync home tab when navigating to a detail route
            LaunchedEffect(route) {
                when (route) {
                    is WiretapRoute.SocketDetail -> homeVm.selectTab(WiretapHomeViewModel.TAB_WEBSOCKET)
                    is WiretapRoute.HttpDetail -> homeVm.selectTab(WiretapHomeViewModel.TAB_HTTP)
                    else -> {}
                }
            }

            val isTwoPaneRoute = route is WiretapRoute.HttpDetail || route is WiretapRoute.SocketDetail

            if (isWideScreen && isTwoPaneRoute) {
                Row(modifier = Modifier.fillMaxSize()) {

                    WiretapHomeScreen(
                        viewModel = homeVm,
                        ruleRepository = ruleRepository,
                        onBack = onBack,
                        onNavigate = { navigateTo(it) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )

                    VerticalDivider(modifier = Modifier.fillMaxHeight())

                    when (val current = route) {
                        is WiretapRoute.HttpDetail -> HttpLogDetailScreen(
                            entry = current.entry,
                            onBack = { navigateTo(null) },
                            onViewRule = { ruleId ->
                                scope.launch {
                                    val rule = ruleRepository.getById(ruleId)
                                    if (rule != null) navigateTo(WiretapRoute.RuleDetail(rule))
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )

                        is WiretapRoute.SocketDetail -> {
                            val vm = viewModel(key = "socket_${current.socketId}") {
                                SocketDetailViewModel(current.socketId, orchestrator)
                            }
                            SocketDetailScreen(
                                viewModel = vm,
                                onBack = { navigateTo(null) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }

                        else -> {}
                    }
                }
            } else {
                when (val current = route) {
                    is WiretapRoute.SocketDetail -> {
                        val vm = viewModel(key = "socket_${current.socketId}") {
                            SocketDetailViewModel(current.socketId, orchestrator)
                        }
                        SocketDetailScreen(
                            viewModel = vm,
                            onBack = { navigateTo(null) },
                        )
                    }

                    is WiretapRoute.HttpDetail -> HttpLogDetailScreen(
                        entry = current.entry,
                        onBack = { navigateTo(null) },
                        onViewRule = { ruleId ->
                            scope.launch {
                                val rule = ruleRepository.getById(ruleId)
                                if (rule != null) navigateTo(WiretapRoute.RuleDetail(rule))
                            }
                        },
                    )

                    is WiretapRoute.RuleDetail -> {
                        val vm = viewModel(key = "rule_detail_${current.rule.id}") {
                            RuleDetailViewModel(current.rule.id, current.rule.enabled, ruleRepository)
                        }
                        RuleDetailScreen(
                            rule = current.rule,
                            viewModel = vm,
                            onBack = { navigateTo(null) },
                            onDeleted = { navigateTo(null) },
                            onEditClick = { navigateTo(WiretapRoute.CreateRule(existingRule = current.rule)) },
                        )
                    }

                    is WiretapRoute.CreateRule -> {
                        val vm = viewModel(
                            key = "create_rule_${current.existingRule?.id}_${current.prefillFromLog?.id}",
                        ) {
                            CreateRuleViewModel(ruleRepository, findConflictingRules, current.existingRule, current.prefillFromLog)
                        }
                        CreateRuleScreen(
                            viewModel = vm,
                            ruleRepository = ruleRepository,
                            onBack = { navigateTo(null) },
                            onSaved = { navigateTo(null) },
                            onEditConflictingRule = { rule -> navigateTo(WiretapRoute.CreateRule(existingRule = rule)) },
                        )
                    }

                    null -> {
                        WiretapHomeScreen(
                            viewModel = homeVm,
                            ruleRepository = ruleRepository,
                            onBack = onBack,
                            onNavigate = { navigateTo(it) },
                        )
                    }
                }
            }
        }
    }
}

private fun WiretapRoute.toSaveKey(): String = when (this) {
    is WiretapRoute.HttpDetail -> "http:${entry.id}"
    is WiretapRoute.SocketDetail -> "socket:$socketId"
    is WiretapRoute.RuleDetail -> "rule:${rule.id}"
    is WiretapRoute.CreateRule -> "create:${existingRule?.id ?: 0}:${prefillFromLog?.id ?: 0}"
}

private suspend fun restoreRoute(
    key: String,
    orchestrator: WiretapOrchestrator,
    ruleRepository: RuleRepository,
): WiretapRoute? {
    val parts = key.split(":")
    return when (parts[0]) {
        "http" -> parts.getOrNull(1)?.toLongOrNull()
            ?.let { orchestrator.getLogById(it) }
            ?.let { WiretapRoute.HttpDetail(it) }

        "socket" -> parts.getOrNull(1)?.toLongOrNull()
            ?.let { WiretapRoute.SocketDetail(it) }

        "rule" -> parts.getOrNull(1)?.toLongOrNull()
            ?.let { ruleRepository.getById(it) }
            ?.let { WiretapRoute.RuleDetail(it) }

        "create" -> {
            val existingRule = parts.getOrNull(1)?.toLongOrNull()
                ?.takeIf { it > 0 }?.let { ruleRepository.getById(it) }
            val prefillLog = parts.getOrNull(2)?.toLongOrNull()
                ?.takeIf { it > 0 }?.let { orchestrator.getLogById(it) }
            WiretapRoute.CreateRule(existingRule, prefillLog)
        }

        else -> null
    }
}
