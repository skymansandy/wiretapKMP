/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.scenes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import dev.skymansandy.wiretap.navigation.compose.LocalWiretapNavigator

private const val BOTTOM_SHEET_KEY = "wiretap_bottom_sheet"

internal fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to true)

internal class BottomSheetScene(
    override val key: Any,
    override val previousEntries: List<NavEntry<NavKey>>,
    private val sheetEntry: NavEntry<NavKey>,
    private val backgroundEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {

    override val entries: List<NavEntry<NavKey>> = backgroundEntries + sheetEntry

    @OptIn(ExperimentalMaterial3Api::class)
    override val content: @Composable () -> Unit = {
        val navigator = LocalWiretapNavigator.current
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        for (entry in backgroundEntries) {
            entry.Content()
        }

        ModalBottomSheet(
            onDismissRequest = { navigator.pop() },
            sheetState = sheetState,
        ) {
            sheetEntry.Content()
        }
    }
}

internal class BottomSheetSceneStrategy : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
        val lastEntry = entries.lastOrNull() ?: return null
        if (!lastEntry.metadata.containsKey(BOTTOM_SHEET_KEY)) return null

        val backgroundEntries = entries.dropLast(1)

        return BottomSheetScene(
            key = lastEntry.contentKey,
            previousEntries = emptyList(),
            sheetEntry = lastEntry,
            backgroundEntries = backgroundEntries,
        )
    }
}
