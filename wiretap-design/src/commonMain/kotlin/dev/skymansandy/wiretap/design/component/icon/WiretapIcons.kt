/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.design.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Wiretap icon set. Each name maps to a Material Icons Extended ImageVector
 * (already a dependency of this module). They render via the standard
 * `Icon(imageVector = WiretapIcons.Search, contentDescription = ...)` call,
 * honoring `LocalContentColor` for tint — same idiom the rest of the codebase
 * already uses.
 *
 * The mapping preserves the design system's icon vocabulary (Search/Filter/
 * Trash/etc.) so callers don't have to know about the Material naming.
 */
object WiretapIcons {
    val Search: ImageVector get() = Icons.Filled.Search
    val Filter: ImageVector get() = Icons.Filled.FilterList
    val Trash: ImageVector get() = Icons.Filled.Delete
    val Back: ImageVector get() = Icons.AutoMirrored.Filled.ArrowBack
    val Close: ImageVector get() = Icons.Filled.Close
    val Chevron: ImageVector get() = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Plus: ImageVector get() = Icons.Filled.Add
    val Share: ImageVector get() = Icons.Filled.Share
    val Copy: ImageVector get() = Icons.Filled.ContentCopy
    val Edit: ImageVector get() = Icons.Filled.Edit
    val More: ImageVector get() = Icons.Filled.MoreHoriz
    val Info: ImageVector get() = Icons.Outlined.Info
    val Lock: ImageVector get() = Icons.Filled.Lock
    val ScrollDown: ImageVector get() = Icons.Filled.KeyboardDoubleArrowDown
    val Http: ImageVector get() = Icons.Filled.Http
    val Ws: ImageVector get() = Icons.AutoMirrored.Filled.CompareArrows
    val Sse: ImageVector get() = Icons.Filled.Podcasts
    val Regex: ImageVector get() = Icons.Filled.Code
    val SetupArrow: ImageVector get() = Icons.AutoMirrored.Filled.ArrowForward
    val EmptyNet: ImageVector get() = Icons.Filled.WifiOff
}

/** Display label for each icon in the showcase grid. */
data class WiretapIconEntry(val name: String, val vector: ImageVector)

val WiretapIconCatalog: List<WiretapIconEntry> = listOf(
    WiretapIconEntry("search", WiretapIcons.Search),
    WiretapIconEntry("filter", WiretapIcons.Filter),
    WiretapIconEntry("trash", WiretapIcons.Trash),
    WiretapIconEntry("back", WiretapIcons.Back),
    WiretapIconEntry("close", WiretapIcons.Close),
    WiretapIconEntry("chevron", WiretapIcons.Chevron),
    WiretapIconEntry("plus", WiretapIcons.Plus),
    WiretapIconEntry("share", WiretapIcons.Share),
    WiretapIconEntry("copy", WiretapIcons.Copy),
    WiretapIconEntry("edit", WiretapIcons.Edit),
    WiretapIconEntry("more", WiretapIcons.More),
    WiretapIconEntry("info", WiretapIcons.Info),
    WiretapIconEntry("lock", WiretapIcons.Lock),
    WiretapIconEntry("scrollDown", WiretapIcons.ScrollDown),
    WiretapIconEntry("http", WiretapIcons.Http),
    WiretapIconEntry("ws", WiretapIcons.Ws),
    WiretapIconEntry("sse", WiretapIcons.Sse),
    WiretapIconEntry("regex", WiretapIcons.Regex),
    WiretapIconEntry("setupArrow", WiretapIcons.SetupArrow),
    WiretapIconEntry("emptyNet", WiretapIcons.EmptyNet),
)
