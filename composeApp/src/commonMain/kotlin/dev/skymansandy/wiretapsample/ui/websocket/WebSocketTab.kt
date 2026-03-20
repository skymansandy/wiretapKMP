package dev.skymansandy.wiretapsample.ui.websocket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.wiretapsample.model.ColorServerError
import dev.skymansandy.wiretapsample.model.ColorSuccess
import dev.skymansandy.wiretapsample.model.wsServers
import dev.skymansandy.wiretapsample.resources.Res
import dev.skymansandy.wiretapsample.resources.connect
import dev.skymansandy.wiretapsample.resources.connected
import dev.skymansandy.wiretapsample.resources.connecting
import dev.skymansandy.wiretapsample.resources.disconnect
import dev.skymansandy.wiretapsample.resources.send
import dev.skymansandy.wiretapsample.resources.type_message
import dev.skymansandy.wiretapsample.resources.websocket_title
import dev.skymansandy.wiretapsample.viewmodel.WebSocketViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebSocketTab(
    viewModel: WebSocketViewModel,
    modifier: Modifier = Modifier,
) {
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnecting.collectAsStateWithLifecycle()
    val selectedServerIndex by viewModel.selectedServerIndex.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems == 0 || lastVisibleItem >= totalItems - 2
        }
    }

    LaunchedEffect(viewModel.messageLog.size) {
        if (viewModel.messageLog.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(viewModel.messageLog.size - 1)
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
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            wsServers.forEachIndexed { index, (_, label) ->
                OutlinedButton(
                    onClick = { viewModel.selectServer(index) },
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
            Button(
                onClick = { viewModel.toggleConnection() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) ColorServerError else ColorSuccess,
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
            items(viewModel.messageLog) { entry ->
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
                        viewModel.sendMessage(text)
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
