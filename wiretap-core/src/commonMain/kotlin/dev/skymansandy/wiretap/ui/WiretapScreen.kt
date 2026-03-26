package dev.skymansandy.wiretap.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.WiretapRule
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val WIDE_SCREEN_BREAKPOINT = 600.dp

private val savedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(WiretapRoute.Home::class, WiretapRoute.Home.serializer())
            subclass(WiretapRoute.HttpDetail::class, WiretapRoute.HttpDetail.serializer())
            subclass(WiretapRoute.SocketDetail::class, WiretapRoute.SocketDetail.serializer())
            subclass(WiretapRoute.RuleDetail::class, WiretapRoute.RuleDetail.serializer())
            subclass(WiretapRoute.CreateRule::class, WiretapRoute.CreateRule.serializer())
        }
    }
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.forwardTransition(): ContentTransform =
    if (targetState is WiretapListDetailScene || initialState is WiretapListDetailScene) {
        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
    } else {
        slideInHorizontally(spring()) { it } togetherWith
            slideOutHorizontally(spring()) { -it }
    }

private fun AnimatedContentTransitionScope<Scene<NavKey>>.popTransition(): ContentTransform =
    if (targetState is WiretapListDetailScene || initialState is WiretapListDetailScene) {
        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
    } else {
        slideInHorizontally(spring()) { -it } togetherWith
            slideOutHorizontally(spring()) { it }
    }

@Composable
fun WiretapScreen(
    onBack: () -> Unit,
    orchestrator: WiretapOrchestrator = WiretapDi.orchestrator,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
    findConflictingRules: FindConflictingRulesUseCase = WiretapDi.findConflictingRules,
    initialSocketId: Long? = null,
    onInitialSocketConsumed: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(savedStateConfig, WiretapRoute.Home)
    val density = LocalDensity.current
    var isWideScreen by rememberSaveable { mutableStateOf(false) }

    val homeVm = viewModel { WiretapHomeViewModel(orchestrator, ruleRepository) }

    val sceneStrategy = remember(isWideScreen) {
        WiretapListDetailSceneStrategy(isWideScreen).then(SinglePaneSceneStrategy())
    }

    // Sync home tab when navigating to a detail route
    val lastKey = backStack.lastOrNull()
    LaunchedEffect(lastKey) {
        when (lastKey) {
            is WiretapRoute.SocketDetail -> homeVm.selectTab(WiretapHomeViewModel.TAB_WEBSOCKET)
            is WiretapRoute.HttpDetail -> homeVm.selectTab(WiretapHomeViewModel.TAB_HTTP)
            else -> {}
        }
    }

    // Handle deep-link to socket detail
    LaunchedEffect(initialSocketId) {
        if (initialSocketId != null) {
            backStack.removeAll { it !is WiretapRoute.Home }
            backStack.add(WiretapRoute.SocketDetail(initialSocketId))
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
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategy = sceneStrategy,
                transitionSpec = { forwardTransition() },
                popTransitionSpec = { popTransition() },
                predictivePopTransitionSpec = { _ -> popTransition() },
                entryProvider = entryProvider {
                    entry<WiretapRoute.Home>(
                        metadata = listPane(),
                    ) {
                        WiretapHomeScreen(
                            viewModel = homeVm,
                            ruleRepository = ruleRepository,
                            onBack = onBack,
                            onNavigate = { route ->
                                if (route != null) {
                                    backStack.removeAll { it !is WiretapRoute.Home }
                                    backStack.add(route)
                                } else {
                                    backStack.removeAll { it !is WiretapRoute.Home }
                                }
                            },
                        )
                    }

                    entry<WiretapRoute.HttpDetail>(
                        metadata = detailPane(),
                    ) { key ->
                        val fullEntry: HttpLogEntry? by produceState<HttpLogEntry?>(null, key.entryId) {
                            value = orchestrator.getHttpLogById(key.entryId)
                        }
                        fullEntry?.let { entry ->
                            HttpLogDetailScreen(
                                entry = entry,
                                onBack = { backStack.removeLastOrNull() },
                                onViewRule = { ruleId ->
                                    backStack.add(WiretapRoute.RuleDetail(ruleId))
                                },
                            )
                        }
                    }

                    entry<WiretapRoute.SocketDetail>(
                        metadata = detailPane(),
                    ) { key ->
                        val vm = viewModel(key = "socket_${key.socketId}") {
                            SocketDetailViewModel(key.socketId, orchestrator)
                        }
                        SocketDetailScreen(
                            viewModel = vm,
                            onBack = { backStack.removeLastOrNull() },
                        )
                    }

                    entry<WiretapRoute.RuleDetail> { key ->
                        val rule: WiretapRule? by produceState<WiretapRule?>(null, key.ruleId) {
                            value = ruleRepository.getById(key.ruleId)
                        }
                        rule?.let { r ->
                            val vm = viewModel(key = "rule_detail_${key.ruleId}") {
                                RuleDetailViewModel(r.id, r.enabled, ruleRepository)
                            }
                            RuleDetailScreen(
                                rule = r,
                                viewModel = vm,
                                onBack = { backStack.removeLastOrNull() },
                                onDeleted = { backStack.removeLastOrNull() },
                                onEditClick = {
                                    backStack.add(
                                        WiretapRoute.CreateRule(existingRuleId = r.id),
                                    )
                                },
                            )
                        }
                    }

                    entry<WiretapRoute.CreateRule> { key ->
                        CreateRuleEntry(
                            key = key,
                            backStack = backStack,
                            ruleRepository = ruleRepository,
                            orchestrator = orchestrator,
                            findConflictingRules = findConflictingRules,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun CreateRuleEntry(
    key: WiretapRoute.CreateRule,
    backStack: MutableList<NavKey>,
    ruleRepository: RuleRepository,
    orchestrator: WiretapOrchestrator,
    findConflictingRules: FindConflictingRulesUseCase,
) {
    var existingRule: WiretapRule? by remember { mutableStateOf(null) }
    var prefillLog: HttpLogEntry? by remember { mutableStateOf(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        existingRule = if (key.existingRuleId > 0) {
            ruleRepository.getById(key.existingRuleId)
        } else {
            null
        }
        prefillLog = if (key.prefillFromLogId > 0) {
            orchestrator.getHttpLogById(key.prefillFromLogId)
        } else {
            null
        }
        loaded = true
    }

    if (loaded) {
        val vm = viewModel(
            key = "create_rule_${key.existingRuleId}_${key.prefillFromLogId}",
        ) {
            CreateRuleViewModel(
                ruleRepository,
                findConflictingRules,
                existingRule,
                prefillLog,
            )
        }
        CreateRuleScreen(
            viewModel = vm,
            ruleRepository = ruleRepository,
            onBack = {
                vm.resetStep()
                backStack.removeLastOrNull()
            },
            onSaved = { savedRule ->
                backStack.removeLastOrNull()
                if (savedRule != null) {
                    backStack.add(WiretapRoute.RuleDetail(savedRule.id))
                }
            },
            onEditConflictingRule = { conflictRule ->
                backStack.removeLastOrNull()
                backStack.add(
                    WiretapRoute.CreateRule(existingRuleId = conflictRule.id),
                )
            },
        )
    }
}
