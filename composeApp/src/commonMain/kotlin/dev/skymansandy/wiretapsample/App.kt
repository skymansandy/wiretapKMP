package dev.skymansandy.wiretapsample

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Stream
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.skymansandy.wiretap.helper.notification.enableWiretapLauncher
import dev.skymansandy.wiretapsample.di.sampleAppModule
import dev.skymansandy.wiretapsample.model.TabItem
import dev.skymansandy.wiretapsample.resources.Res
import dev.skymansandy.wiretapsample.resources.tab_http
import dev.skymansandy.wiretapsample.resources.tab_websocket
import dev.skymansandy.wiretapsample.ui.http.HttpTab
import dev.skymansandy.wiretapsample.ui.scaffold.LandscapeLayout
import dev.skymansandy.wiretapsample.ui.scaffold.PortraitLayout
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import dev.skymansandy.wiretapsample.ui.websocket.WebSocketTab
import dev.skymansandy.wiretapsample.viewmodel.HttpViewModel
import dev.skymansandy.wiretapsample.viewmodel.WebSocketViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(sampleAppModule)
        },
    ) {
        LaunchedEffect(Unit) {
            enableWiretapLauncher()
        }

        WiretapTheme {
            var selectedTab by remember { mutableIntStateOf(0) }

            val tabs = listOf(
                TabItem(
                    icon = Icons.Default.Http,
                    label = stringResource(Res.string.tab_http),
                ),
                TabItem(
                    icon = Icons.Default.Stream,
                    label = stringResource(Res.string.tab_websocket),
                ),
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
            ) {
                val isLandscape = maxWidth > maxHeight

                if (isLandscape) {
                    LandscapeLayout(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        content = { modifier ->
                            TabContent(
                                modifier = modifier,
                                selectedTab = selectedTab,
                            )
                        },
                    )
                } else {
                    PortraitLayout(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        content = { modifier ->
                            TabContent(
                                modifier = modifier,
                                selectedTab = selectedTab,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    modifier: Modifier,
    selectedTab: Int,
) {
    when (selectedTab) {
        0 -> HttpTab(
            modifier = modifier,
            viewModel = koinViewModel<HttpViewModel>(),
        )

        1 -> WebSocketTab(
            modifier = modifier,
            viewModel = koinViewModel<WebSocketViewModel>(),
        )
    }
}
