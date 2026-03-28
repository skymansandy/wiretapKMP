package dev.skymansandy.wiretap.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WiretapTopBar(
    modifier: Modifier = Modifier,
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    showClearAction: Boolean,
    showFilterAction: Boolean = false,
    activeFilterCount: Int = 0,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onFilter: () -> Unit = {},
    onClear: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            if (isSearchActive) {
                SearchField(
                    modifier = Modifier.focusRequester(searchFocusRequester),
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                )
            } else {
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                if (isSearchActive) {
                    onSearchActiveChange(false)
                } else {
                    onBack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchActiveChange(false) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                    )
                }
            } else {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
            }
            if (showFilterAction) {
                IconButton(onClick = onFilter) {
                    if (activeFilterCount > 0) {
                        BadgedBox(badge = { Badge { Text("$activeFilterCount") } }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                        )
                    }
                }
            }
            if (showClearAction) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear logs",
                    )
                }
            }
        },
    )
}
