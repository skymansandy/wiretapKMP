package dev.skymansandy.wiretap.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

internal val LocalBackButtonVisibility = compositionLocalOf { true }

private const val LIST_PANE_KEY = "wiretap_list_pane"
private const val DETAIL_PANE_KEY = "wiretap_detail_pane"

internal fun listPane(): Map<String, Any> = mapOf(LIST_PANE_KEY to true)
internal fun detailPane(): Map<String, Any> = mapOf(DETAIL_PANE_KEY to true)

internal class WiretapListDetailScene(
    override val key: Any,
    override val previousEntries: List<NavEntry<NavKey>>,
    val listEntry: NavEntry<NavKey>,
    val detailEntry: NavEntry<NavKey>,
) : Scene<NavKey> {

    override val entries: List<NavEntry<NavKey>> = listOf(listEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        CompositionLocalProvider(LocalBackButtonVisibility provides false) {
            Row(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    listEntry.Content()
                }

                VerticalDivider(modifier = Modifier.fillMaxHeight())

                AnimatedContent(
                    targetState = detailEntry,
                    contentKey = { it.contentKey },
                    transitionSpec = {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .consumeWindowInsets(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                        ),
                ) { entry ->
                    entry.Content()
                }
            }
        }
    }
}

internal class WiretapListDetailSceneStrategy(
    private val isWideScreen: Boolean,
) : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
        if (!isWideScreen) return null

        val detailEntry = entries.lastOrNull()
            ?.takeIf { it.metadata.containsKey(DETAIL_PANE_KEY) }
            ?: return null

        val listEntry = entries.findLast { it.metadata.containsKey(LIST_PANE_KEY) }
            ?: return null

        // Use list entry's key so the scene itself is stable; detail animates inside
        return WiretapListDetailScene(
            key = listEntry.contentKey,
            previousEntries = entries.filter { it != listEntry && it != detailEntry },
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}
