package dev.skymansandy.wiretapsample

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.skymansandy.wiretap.helper.notification.enableLaunchTool
import org.jetbrains.compose.resources.stringResource
import wiretapkmp.composeapp.generated.resources.*
import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin
import dev.skymansandy.wiretap.plugin.WiretapKtorWebSocketPlugin
import dev.skymansandy.wiretapsample.ui.http.HttpTab
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import dev.skymansandy.wiretapsample.ui.websocket.WebSocketTab
import dev.skymansandy.wiretapsample.viewmodel.WebSocketViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineExceptionHandler

@Composable
fun App() {
    LaunchedEffect(Unit) { enableLaunchTool() }
    WiretapTheme {
        val client = remember {
            HttpClient {
                install(WebSockets)
                install(WiretapKtorWebSocketPlugin)
                install(HttpTimeout)
                install(WiretapKtorPlugin)
            }
        }
        val scope = rememberCoroutineScope {
            CoroutineExceptionHandler { _, _ -> }
        }
        val webSocketViewModel = remember { WebSocketViewModel(client, scope) }
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Http, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_http)) },
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Stream, contentDescription = null) },
                        label = { Text(stringResource(Res.string.tab_websocket)) },
                    )
                }
            },
        ) { padding ->
            when (selectedTab) {
                0 -> HttpTab(client = client, modifier = Modifier.padding(padding))
                1 -> WebSocketTab(viewModel = webSocketViewModel, modifier = Modifier.padding(padding))
            }
        }
    }
}
