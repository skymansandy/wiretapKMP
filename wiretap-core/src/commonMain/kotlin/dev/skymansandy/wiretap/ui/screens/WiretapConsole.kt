package dev.skymansandy.wiretap.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.orchestrator.SocketLogManager
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.CreateRuleScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HomeScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HttpDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.RuleDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.SocketDetailScreen
import dev.skymansandy.wiretap.navigation.api.screenSerializersModule
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.navigation.impl.BackStackNavigatorImpl
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.scenes.WiretapListDetailSceneStrategy
import dev.skymansandy.wiretap.ui.scenes.detailPane
import dev.skymansandy.wiretap.ui.scenes.listPane
import dev.skymansandy.wiretap.ui.screens.home.WiretapHomeScreen
import dev.skymansandy.wiretap.ui.screens.home.WiretapHomeViewModel
import dev.skymansandy.wiretap.ui.screens.http.detail.HttpLogDetailScreen
import dev.skymansandy.wiretap.ui.screens.http.detail.HttpLogDetailViewModel
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleScreen
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel
import dev.skymansandy.wiretap.ui.screens.rules.view.RuleDetailScreen
import dev.skymansandy.wiretap.ui.screens.rules.view.RuleDetailViewModel
import dev.skymansandy.wiretap.ui.screens.socket.detail.SocketDetailScreen
import dev.skymansandy.wiretap.ui.screens.socket.detail.SocketDetailViewModel

private val WIDE_SCREEN_BREAKPOINT = 600.dp

/**
 * Builds the synthetic back stack for deep-link navigation following the Nav3 recipe pattern.
 * When a deep-link target is provided, the back stack is pre-populated with
 * `[HomeScreen, target]` so that pressing Back returns to the home screen.
 */
private fun buildSyntheticBackStack(
    deepLinkScreen: WiretapScreen? = null,
): Array<NavKey> = buildList<NavKey> {
    add(HomeScreen)
    if (deepLinkScreen != null) add(deepLinkScreen)
}.toTypedArray()

@Composable
internal fun WiretapConsole(
    onBack: () -> Unit,
    httpLogManager: HttpLogManager = WiretapDi.httpLogManager,
    socketLogManager: SocketLogManager = WiretapDi.socketLogManager,
    ruleRepository: RuleRepository = WiretapDi.ruleRepository,
    findConflictingRules: FindConflictingRulesUseCase = WiretapDi.findConflictingRules,
    deepLinkScreen: WiretapScreen? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    // Nav3 deep-link recipe: initialize the back stack with a synthetic stack
    // that includes the deep-link destination, so Back navigates naturally.
    val initialKeys = remember { buildSyntheticBackStack(deepLinkScreen) }
    val backStack = rememberNavBackStack(screenSerializersModule, *initialKeys)
    val navigator = remember(backStack) { BackStackNavigatorImpl(backStack) }
    val density = LocalDensity.current
    var isWideScreen by rememberSaveable { mutableStateOf(false) }

    val homeVm = viewModel {
        WiretapHomeViewModel(
            httpLogManager = httpLogManager,
            socketLogManager = socketLogManager,
            ruleRepository = ruleRepository,
        )
    }

    val sceneStrategy = remember(isWideScreen) {
        WiretapListDetailSceneStrategy(isWideScreen).then(SinglePaneSceneStrategy())
    }

    // Sync home tab when navigating to a detail route
    val lastKey = backStack.lastOrNull()
    LaunchedEffect(lastKey) {
        when (lastKey) {
            is SocketDetailScreen -> homeVm.selectTab(WiretapHomeViewModel.TAB_WEBSOCKET)
            is HttpDetailScreen -> homeVm.selectTab(WiretapHomeViewModel.TAB_HTTP)
            else -> {}
        }
    }

    // Handle subsequent deep-links (e.g. onNewIntent) after the initial composition
    LaunchedEffect(deepLinkScreen) {
        if (deepLinkScreen != null && backStack.lastOrNull() != deepLinkScreen) {
            navigator.pushDetailPane(deepLinkScreen)
            onDeepLinkConsumed()
        }
    }

    CompositionLocalProvider(
        LocalWideScreen provides isWideScreen,
        LocalWiretapNavigator provides navigator,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    isWideScreen = with(density) { size.width.toDp() } >= WIDE_SCREEN_BREAKPOINT
                },
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.pop() },
                sceneStrategy = sceneStrategy,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<HomeScreen>(
                        metadata = listPane(),
                    ) {
                        WiretapHomeScreen(
                            viewModel = homeVm,
                            ruleRepository = ruleRepository,
                            onBack = onBack,
                        )
                    }

                    entry<HttpDetailScreen>(
                        metadata = detailPane(),
                    ) { key ->
                        val vm = viewModel {
                            HttpLogDetailViewModel(key.entryId, httpLogManager)
                        }
                        val entry by vm.entry.collectAsStateWithLifecycle()
                        entry?.let {
                            HttpLogDetailScreen(entry = it)
                        }
                    }

                    entry<SocketDetailScreen>(
                        metadata = detailPane(),
                    ) { key ->
                        val vm = viewModel {
                            SocketDetailViewModel(key.socketId, socketLogManager)
                        }
                        SocketDetailScreen(viewModel = vm)
                    }

                    entry<RuleDetailScreen>(
                        metadata = detailPane(),
                    ) { key ->
                        val vm = viewModel {
                            RuleDetailViewModel(key.ruleId, ruleRepository)
                        }
                        RuleDetailScreen(viewModel = vm)
                    }

                    entry<CreateRuleScreen>(
                        metadata = detailPane(),
                    ) { key ->
                        val vm = viewModel {
                            CreateRuleViewModel(
                                existingRuleId = key.existingRuleId,
                                prefillFromLogId = key.prefillFromLogId,
                                ruleRepository = ruleRepository,
                                httpLogManager = httpLogManager,
                                findConflictingRules = findConflictingRules,
                            )
                        }
                        val loaded by vm.loaded.collectAsStateWithLifecycle()
                        if (loaded) {
                            CreateRuleScreen(
                                viewModel = vm,
                                ruleRepository = ruleRepository,
                            )
                        }
                    }
                },
            )
        }
    }
}
