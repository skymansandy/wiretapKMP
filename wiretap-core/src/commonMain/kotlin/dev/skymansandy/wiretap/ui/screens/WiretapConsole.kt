/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import dev.skymansandy.wiretap.di.WiretapKoinContext
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.CreateRuleScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HomeScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.HttpDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.RuleDetailScreen
import dev.skymansandy.wiretap.navigation.api.WiretapScreen.SocketDetailScreen
import dev.skymansandy.wiretap.navigation.api.screenSerializersModule
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator
import dev.skymansandy.wiretap.navigation.impl.BackStackNavigatorImpl
import dev.skymansandy.wiretap.navigation.impl.buildSyntheticBackStack
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.common.WIDE_SCREEN_BREAKPOINT
import dev.skymansandy.wiretap.ui.model.HomeTab
import dev.skymansandy.wiretap.ui.scenes.WiretapListDetailSceneStrategy
import dev.skymansandy.wiretap.ui.scenes.detailPane
import dev.skymansandy.wiretap.ui.scenes.listPane
import dev.skymansandy.wiretap.ui.screens.home.WiretapHomeScreen
import dev.skymansandy.wiretap.ui.screens.http.detail.HttpLogDetailScreen
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleScreenView
import dev.skymansandy.wiretap.ui.screens.rules.view.RuleDetailScreenView
import dev.skymansandy.wiretap.ui.screens.socket.detail.SocketDetailScreenView
import org.koin.compose.KoinIsolatedContext

@Composable
internal fun WiretapConsole(
    deepLinkScreen: WiretapScreen? = null,
    onDeepLinkConsumed: () -> Unit = {},
    onBack: () -> Unit,
) {
    // Nav3 deep-link recipe: initialize the back stack with a synthetic stack
    // that includes the deep-link destination, so Back navigates naturally.
    val initialKeys = remember { buildSyntheticBackStack(deepLinkScreen) }
    val backStack = rememberNavBackStack(screenSerializersModule, *initialKeys)
    val navigator = remember(backStack) { BackStackNavigatorImpl(backStack) }
    val density = LocalDensity.current
    var isWideScreen by rememberSaveable { mutableStateOf(false) }
    val sceneStrategy = remember(isWideScreen) {
        WiretapListDetailSceneStrategy(isWideScreen).then(SinglePaneSceneStrategy())
    }

    // Handle subsequent deep-links (e.g. onNewIntent) after the initial composition
    LaunchedEffect(deepLinkScreen) {
        if (deepLinkScreen != null && backStack.lastOrNull() != deepLinkScreen) {
            navigator.pushDetailPane(deepLinkScreen)
            onDeepLinkConsumed()
        }
    }

    KoinIsolatedContext(context = WiretapKoinContext.koinApp) {
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
                    onBack = {
                        if (backStack.size <= 1) onBack() else navigator.pop()
                    },
                    sceneStrategy = sceneStrategy,
                    transitionSpec = { wiretapSlideTransition(isWideScreen, isPop = false) },
                    popTransitionSpec = { wiretapSlideTransition(isWideScreen, isPop = true) },
                    predictivePopTransitionSpec = { wiretapSlideTransition(isWideScreen, isPop = true) },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<HomeScreen>(
                            metadata = listPane(),
                        ) {
                            val initialTab = when (backStack.lastOrNull()) {
                                is HttpDetailScreen -> HomeTab.Http
                                is RuleDetailScreen,
                                is CreateRuleScreen,
                                -> HomeTab.Http
                                is SocketDetailScreen -> HomeTab.WebSocket
                                else -> null
                            }

                            WiretapHomeScreen(
                                initialTab = initialTab,
                            )
                        }

                        entry<HttpDetailScreen>(
                            metadata = detailPane(),
                        ) { key ->
                            HttpLogDetailScreen(
                                entryId = key.entryId,
                            )
                        }

                        entry<SocketDetailScreen>(
                            metadata = detailPane(),
                        ) { key ->
                            SocketDetailScreenView(
                                socketId = key.socketId,
                            )
                        }

                        entry<RuleDetailScreen>(
                            metadata = detailPane(),
                        ) { key ->
                            RuleDetailScreenView(
                                ruleId = key.ruleId,
                            )
                        }

                        entry<CreateRuleScreen>(
                            metadata = detailPane(),
                        ) { key ->
                            CreateRuleScreenView(
                                existingRuleId = key.existingRuleId,
                                prefillFromLogId = key.prefillFromLogId,
                            )
                        }
                    },
                )
            }
        }
    }
}

private fun wiretapSlideTransition(isWideScreen: Boolean, isPop: Boolean): ContentTransform =
    when {
        isWideScreen -> ContentTransform(EnterTransition.None, ExitTransition.None)
        isPop -> slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        else -> slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
    }
