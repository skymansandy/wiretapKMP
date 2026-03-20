package dev.skymansandy.wiretap.ui.screens.console.http.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.domain.model.ResponseSource
import dev.skymansandy.wiretap.resources.Res
import dev.skymansandy.wiretap.resources.create_rule_swipe
import dev.skymansandy.wiretap.resources.view_rule_swipe
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val RevealWidth = 64.dp

@Composable
internal fun SwipeableNetworkLogItem(
    modifier: Modifier = Modifier,
    entry: HttpLogEntry,
    searchQuery: String,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onCollapse: () -> Unit,
    onClick: () -> Unit,
    onCreateRule: () -> Unit,
    onViewRule: () -> Unit,
) {
    val hasMatchedRule = entry.source != ResponseSource.Network && entry.matchedRuleId != null
    val revealWidthPx = with(LocalDensity.current) { RevealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Sync external isRevealed -> animation
    LaunchedEffect(isRevealed) {
        val target = if (isRevealed) -revealWidthPx else 0f
        if (offsetX.value != target) {
            offsetX.animateTo(target)
        }
    }

    val bgColor = when {
        hasMatchedRule -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        hasMatchedRule -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Action revealed behind the item, pinned to end
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .matchParentSize()
                .background(bgColor)
                .clickable { if (hasMatchedRule) onViewRule() else onCreateRule() },
        ) {
            Column(
                modifier = Modifier.width(RevealWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (hasMatchedRule) Icons.Default.Visibility else Icons.Default.Add,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )

                Text(
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    text = when {
                        hasMatchedRule -> stringResource(Res.string.view_rule_swipe)
                        else -> stringResource(Res.string.create_rule_swipe)
                    },
                )
            }
        }

        // Foreground content that slides
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                // Snap to revealed or closed based on how far user dragged
                                if (offsetX.value < -revealWidthPx / 2) {
                                    offsetX.animateTo(-revealWidthPx)
                                    onReveal()
                                } else {
                                    offsetX.animateTo(0f)
                                    onCollapse()
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f)
                                onCollapse()
                            }
                        },
                    ) { _, dragAmount ->
                        scope.launch {
                            val newValue = (offsetX.value + dragAmount)
                                .coerceIn(-revealWidthPx, 0f)
                            offsetX.snapTo(newValue)
                        }
                    }
                },
        ) {
            NetworkLogItemContent(
                entry = entry,
                searchQuery = searchQuery,
                onClick = onClick,
            )
        }
    }
}
