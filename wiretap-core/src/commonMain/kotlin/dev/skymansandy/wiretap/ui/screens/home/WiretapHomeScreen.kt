package dev.skymansandy.wiretap.ui.screens.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.ui.common.LocalWideScreen
import dev.skymansandy.wiretap.ui.model.HomeTab
import dev.skymansandy.wiretap.ui.screens.http.list.HttpTabScreen
import dev.skymansandy.wiretap.ui.screens.socket.list.SocketTabScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun WiretapHomeScreen(
    modifier: Modifier = Modifier,
    initialTab: HomeTab? = null,
    viewModel: WiretapHomeViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val isWideScreen = LocalWideScreen.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    // Sync home tab when navigating to a detail route
    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            viewModel.selectTab(initialTab)
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = when (isWideScreen) {
            false -> ScaffoldDefaults.contentWindowInsets.exclude(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
            )

            true -> ScaffoldDefaults.contentWindowInsets.exclude(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
        },
        bottomBar = {
            // / if its widescreen, a navigation rail is sent to tab composables to render.
            if (!isWideScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == HomeTab.Http,
                        onClick = { viewModel.selectTab(HomeTab.Http) },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text("HTTP") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == HomeTab.WebSocket,
                        onClick = { viewModel.selectTab(HomeTab.WebSocket) },
                        icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        label = { Text("WebSocket") },
                    )
                }
            }
        },
    ) { padding ->

        val navigationRail: (@Composable () -> Unit)? = when {
            isWideScreen -> {
                {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.Http,
                            onClick = { viewModel.selectTab(HomeTab.Http) },
                            icon = { Icon(Icons.Default.Http, contentDescription = null) },
                            label = { Text("HTTP") },
                        )
                        NavigationRailItem(
                            selected = selectedTab == HomeTab.WebSocket,
                            onClick = { viewModel.selectTab(HomeTab.WebSocket) },
                            icon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                            label = { Text("WebSocket") },
                        )
                    }
                }
            }
            else -> {
                null
            }
        }

        when (selectedTab) {
            HomeTab.Http -> HttpTabScreen(
                modifier = Modifier.fillMaxSize().padding(padding),
                navigationRail = navigationRail,
                onBack = onBack,
            )

            HomeTab.WebSocket -> SocketTabScreen(
                modifier = Modifier.fillMaxSize().padding(padding),
                navigationRail = navigationRail,
                onBack = onBack,
            )
        }
    }
}
