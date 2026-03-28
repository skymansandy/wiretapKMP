package dev.skymansandy.wiretap.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
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
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    searchFocusRequester: FocusRequester,
    showClearAction: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {

    TopAppBar(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isSearchActive) {
                IconButton(onClick = { onSearchActiveChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close search")
                }
            } else {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
            if (showClearAction) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs")
                }
            }
        },
    )
}
