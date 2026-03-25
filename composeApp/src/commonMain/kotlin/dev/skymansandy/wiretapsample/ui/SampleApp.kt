package dev.skymansandy.wiretapsample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretap.helper.launcher.enableWiretapLauncher
import dev.skymansandy.wiretapsample.model.ActionCategory
import dev.skymansandy.wiretapsample.model.HttpSampleActions
import dev.skymansandy.wiretapsample.model.SampleAction
import dev.skymansandy.wiretapsample.model.SampleMessage
import dev.skymansandy.wiretapsample.model.TabItem
import dev.skymansandy.wiretapsample.model.WsSampleActions
import dev.skymansandy.wiretapsample.resources.Res
import dev.skymansandy.wiretapsample.resources.connect
import dev.skymansandy.wiretapsample.resources.connected
import dev.skymansandy.wiretapsample.resources.connecting
import dev.skymansandy.wiretapsample.resources.disconnect
import dev.skymansandy.wiretapsample.resources.http_requests
import dev.skymansandy.wiretapsample.resources.received_indicator
import dev.skymansandy.wiretapsample.resources.send
import dev.skymansandy.wiretapsample.resources.sent_indicator
import dev.skymansandy.wiretapsample.resources.status_label
import dev.skymansandy.wiretapsample.resources.status_ready
import dev.skymansandy.wiretapsample.resources.tab_http
import dev.skymansandy.wiretapsample.resources.tab_websocket
import dev.skymansandy.wiretapsample.resources.type_message
import dev.skymansandy.wiretapsample.resources.websocket_title
import dev.skymansandy.wiretapsample.ui.scaffold.LandscapeLayout
import dev.skymansandy.wiretapsample.ui.scaffold.PortraitLayout
import dev.skymansandy.wiretapsample.ui.theme.ColorServerError
import dev.skymansandy.wiretapsample.ui.theme.ColorSuccess
import dev.skymansandy.wiretapsample.ui.theme.ColorWsSent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun SampleApp(
    title: String = "",
    httpActions: HttpSampleActions,
    wsActions: WsSampleActions,
) {
    LaunchedEffect(Unit) {
        enableWiretapLauncher()
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabItem(
            icon = Icons.Default.Http,
            label = stringResource(Res.string.tab_http),
        ),
        TabItem(
            icon = Icons.Default.Wifi,
            label = stringResource(Res.string.tab_websocket),
        ),
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            LandscapeLayout(
                title = title,
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                content = { modifier ->
                    TabContent(
                        modifier = modifier,
                        selectedTab = selectedTab,
                        httpActions = httpActions,
                        wsActions = wsActions,
                    )
                },
            )
        } else {
            PortraitLayout(
                title = title,
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                content = { modifier ->
                    TabContent(
                        modifier = modifier,
                        selectedTab = selectedTab,
                        httpActions = httpActions,
                        wsActions = wsActions,
                    )
                },
            )
        }
    }
}

@Composable
private fun TabContent(
    modifier: Modifier,
    selectedTab: Int,
    httpActions: HttpSampleActions,
    wsActions: WsSampleActions,
) {

    when (selectedTab) {
        0 -> HttpTab(modifier = modifier, httpActions = httpActions)
        1 -> WsTab(modifier = modifier, wsActions = wsActions)
    }
}

// region HTTP Tab

@Composable
private fun HttpTab(
    httpActions: HttpSampleActions,
    modifier: Modifier = Modifier,
) {

    val statusLog by httpActions.statusLog.collectAsStateWithLifecycle()
    val readyText = stringResource(Res.string.status_ready)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Text(
            text = stringResource(Res.string.http_requests),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val isWide = maxWidth > 600.dp

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActionButtonGrid(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        httpActions = httpActions,
                    )
                    StatusWindow(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        statusLog = statusLog.ifEmpty { readyText },
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatusWindow(
                        modifier = Modifier.weight(1f),
                        statusLog = statusLog.ifEmpty { readyText },
                    )
                    ActionButtonGrid(
                        modifier = Modifier.weight(1f),
                        httpActions = httpActions,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionButtonGrid(
    httpActions: HttpSampleActions,
    modifier: Modifier = Modifier,
) {

    val groups = remember(httpActions.actions) { groupActions(httpActions.actions) }
    val pagerState = rememberPagerState { groups.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {

        SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
            groups.forEachIndexed { index, group ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(group.title) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->

            FlowRow(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                groups[page].actions.forEach { (index, action) ->
                    OutlinedButton(
                        onClick = { httpActions.executeAction(index) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = action.color),
                    ) {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private data class ActionGroup(
    val title: String,
    val actions: List<Pair<Int, SampleAction>>,
)

private fun groupActions(actions: List<SampleAction>): List<ActionGroup> {
    return actions.mapIndexed { index, action -> index to action }
        .groupBy { (_, action) ->
            when (action.category) {
                ActionCategory.Success -> "Success"
                ActionCategory.Redirect,
                ActionCategory.ClientError,
                ActionCategory.ServerError,
                -> "!Success"

                ActionCategory.Timeout,
                ActionCategory.Cancel,
                -> "Timeouts"

                ActionCategory.Batch -> "Burst"
            }
        }
        .map { (title, items) -> ActionGroup(title, items) }
}

@Composable
private fun StatusWindow(
    statusLog: String,
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val scrollState = rememberScrollState()

        LaunchedEffect(statusLog) {
            scrollState.animateScrollTo(0)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
        ) {

            Text(
                text = stringResource(Res.string.status_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = statusLog,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// endregion

// region WebSocket Tab

@Composable
private fun WsTab(
    wsActions: WsSampleActions,
    modifier: Modifier = Modifier,
) {

    val isConnected by wsActions.isConnected.collectAsStateWithLifecycle()
    val isConnecting by wsActions.isConnecting.collectAsStateWithLifecycle()
    val selectedServerIndex by wsActions.selectedServerIndex.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems == 0 || lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(wsActions.messageLog.size) {
        if (wsActions.messageLog.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(wsActions.messageLog.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Text(
            text = stringResource(Res.string.websocket_title),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            wsActions.servers.forEachIndexed { index, (_, label) ->
                OutlinedButton(
                    onClick = { wsActions.selectServer(index) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = when (selectedServerIndex) {
                            index -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        },
                    ),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            OutlinedButton(
                onClick = { wsActions.toggleConnection() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isConnected) ColorServerError else ColorSuccess,
                ),
                enabled = !isConnecting,
            ) {
                Text(
                    text = when {
                        isConnecting -> stringResource(Res.string.connecting)
                        isConnected -> stringResource(Res.string.disconnect)
                        else -> stringResource(Res.string.connect)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            if (isConnected) {
                Text(
                    text = stringResource(Res.string.connected),
                    style = MaterialTheme.typography.labelMedium,
                    color = ColorSuccess,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(wsActions.messageLog) { entry ->
                WsMessageItem(entry)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(Res.string.type_message)) },
                singleLine = true,
                enabled = isConnected,
            )

            IconButton(
                onClick = {
                    val text = messageText.trim()
                    if (text.isNotEmpty() && isConnected) {
                        messageText = ""
                        wsActions.sendMessage(text)
                    }
                },
                enabled = isConnected && messageText.isNotBlank(),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(Res.string.send),
                    tint = when {
                        isConnected && messageText.isNotBlank() -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                )
            }
        }
    }
}

@Composable
private fun WsMessageItem(entry: SampleMessage) {

    val (bgColor, textColor, alignment) = when (entry.type) {
        SampleMessage.MessageType.Sent -> Triple(
            ColorWsSent.copy(alpha = 0.15f),
            ColorWsSent,
            Alignment.CenterEnd,
        )

        SampleMessage.MessageType.Received -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            Alignment.CenterStart,
        )

        SampleMessage.MessageType.System -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Alignment.Center,
        )
    }

    if (entry.type == SampleMessage.MessageType.System) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            text = entry.text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontFamily = FontFamily.Monospace,
        )
    } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
            contentAlignment = alignment,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {

                Text(
                    text = if (entry.type == SampleMessage.MessageType.Sent) {
                        stringResource(Res.string.sent_indicator)
                    } else {
                        stringResource(Res.string.received_indicator)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f),
                )

                Text(
                    text = entry.text,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = textColor,
                )
            }
        }
    }
}

// endregion
