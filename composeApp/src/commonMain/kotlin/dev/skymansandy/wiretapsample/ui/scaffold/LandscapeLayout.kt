package dev.skymansandy.wiretapsample.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.skymansandy.wiretapsample.model.TabItem

@Composable
fun LandscapeLayout(
    tabs: List<TabItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail {
            tabs.forEachIndexed { index, tab ->
                NavigationRailItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                )
            }
        }

        content(Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun LandscapeLayoutPreview() {
    MaterialTheme {
        LandscapeLayout(
            tabs = listOf(
                TabItem(icon = Icons.Default.Http, label = "HTTP"),
                TabItem(icon = Icons.Default.Stream, label = "WebSocket"),
            ),
            selectedTab = 0,
            onTabSelected = {},
            content = { modifier ->
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Tab Content")
                }
            },
        )
    }
}
