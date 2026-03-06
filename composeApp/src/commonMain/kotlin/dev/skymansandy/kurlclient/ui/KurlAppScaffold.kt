package dev.skymansandy.kurlclient.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.skymansandy.kurlclient.ui.adaptive.WindowWidthClass
import dev.skymansandy.kurlclient.ui.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.ui.collections.CollectionsScreen
import dev.skymansandy.kurlclient.ui.collections.CollectionsViewModel
import dev.skymansandy.kurlclient.ui.request.RequestPanel
import dev.skymansandy.kurlclient.ui.request.SaveRequestDialog
import dev.skymansandy.kurlclient.ui.response.ResponsePanel

private enum class NavDestination(val label: String) {
    New("New"), Collections("Collections"), History("History")
}

@Composable
fun KurlAppScaffold() {
    val vm = viewModel<RequestViewModel> { RequestViewModel() }
    val collectionsVm = viewModel<CollectionsViewModel> { CollectionsViewModel() }

    var selectedNav by remember { mutableStateOf(NavDestination.New) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on successful save
    LaunchedEffect(vm.saveSuccess) {
        if (vm.saveSuccess) {
            collectionsVm.refresh()
            snackbarHostState.showSnackbar("Request saved to collections")
            vm.clearSaveSuccess()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowClass = maxWidth.toWindowWidthClass()

        if (showSaveDialog) {
            SaveRequestDialog(
                initialName = if (vm.url.isNotBlank()) vm.url.substringAfterLast("/").take(40) else "Untitled",
                folders = collectionsVm.allFolders,
                folderPaths = collectionsVm.folderPaths,
                onSave = { name, folderId ->
                    vm.saveRequest(name, folderId)
                    showSaveDialog = false
                },
                onCreateFolder = { name, parentId ->
                    collectionsVm.createFolder(name, parentId)
                },
                onDismiss = { showSaveDialog = false }
            )
        }

        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                vm = vm,
                collectionsVm = collectionsVm,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSave = { showSaveDialog = true },
                onRequestSelected = { saved ->
                    vm.loadSavedRequest(saved)
                    selectedNav = NavDestination.New
                }
            )
            else -> ExpandedScaffold(
                vm = vm,
                collectionsVm = collectionsVm,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSave = { showSaveDialog = true },
                onRequestSelected = { saved ->
                    vm.loadSavedRequest(saved)
                    selectedNav = NavDestination.New
                }
            )
        }
    }
}

// ── Mobile layout ─────────────────────────────────────────────────────────────

@Composable
private fun CompactScaffold(
    vm: RequestViewModel,
    collectionsVm: CollectionsViewModel,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (NavDestination) -> Unit,
    onSave: () -> Unit,
    onRequestSelected: (dev.skymansandy.kurlclient.db.SavedRequest) -> Unit
) {
    Scaffold(
        bottomBar = { KurlNavigationBar(selected = selectedNav, onSelect = onNavSelect) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedNav) {
                NavDestination.New -> Column(modifier = Modifier.fillMaxSize()) {
                    RequestPanel(
                        url = vm.url,
                        method = vm.method,
                        params = vm.params,
                        headers = vm.headers,
                        body = vm.body,
                        isLoading = vm.isLoading,
                        onUrlChange = vm::setRequestUrl,
                        onMethodChange = vm::setRequestMethod,
                        onParamUpdate = vm::updateParam,
                        onParamAdd = vm::addParam,
                        onParamRemove = vm::removeParam,
                        onHeaderUpdate = vm::updateHeader,
                        onHeaderAdd = vm::addHeader,
                        onHeaderRemove = vm::removeHeader,
                        onBodyChange = vm::setRequestBody,
                        onSend = vm::sendRequest,
                        onSave = onSave,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    HorizontalDivider()
                    ResponsePanel(
                        response = vm.response,
                        error = vm.error,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
                NavDestination.Collections -> CollectionsScreen(
                    vm = collectionsVm,
                    onRequestSelected = onRequestSelected,
                    modifier = Modifier.fillMaxSize()
                )
                NavDestination.History -> HistoryPlaceholder()
            }
        }
    }
}

// ── Desktop layout ────────────────────────────────────────────────────────────

@Composable
private fun ExpandedScaffold(
    vm: RequestViewModel,
    collectionsVm: CollectionsViewModel,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (NavDestination) -> Unit,
    onSave: () -> Unit,
    onRequestSelected: (dev.skymansandy.kurlclient.db.SavedRequest) -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            KurlNavigationRail(selected = selectedNav, onSelect = onNavSelect)
            VerticalDivider()

            when (selectedNav) {
                NavDestination.New -> Row(modifier = Modifier.fillMaxSize()) {
                    RequestPanel(
                        url = vm.url,
                        method = vm.method,
                        params = vm.params,
                        headers = vm.headers,
                        body = vm.body,
                        isLoading = vm.isLoading,
                        onUrlChange = vm::setRequestUrl,
                        onMethodChange = vm::setRequestMethod,
                        onParamUpdate = vm::updateParam,
                        onParamAdd = vm::addParam,
                        onParamRemove = vm::removeParam,
                        onHeaderUpdate = vm::updateHeader,
                        onHeaderAdd = vm::addHeader,
                        onHeaderRemove = vm::removeHeader,
                        onBodyChange = vm::setRequestBody,
                        onSend = vm::sendRequest,
                        onSave = onSave,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    VerticalDivider()
                    ResponsePanel(
                        response = vm.response,
                        error = vm.error,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                NavDestination.Collections -> CollectionsScreen(
                    vm = collectionsVm,
                    onRequestSelected = onRequestSelected,
                    modifier = Modifier.fillMaxSize()
                )
                NavDestination.History -> HistoryPlaceholder()
            }
        }
    }
}

// ── Navigation components ─────────────────────────────────────────────────────

@Composable
private fun KurlNavigationBar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    NavigationBar {
        NavDestination.entries.forEach { dest ->
            NavigationBarItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun KurlNavigationRail(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        NavDestination.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun NavIcon(dest: NavDestination) {
    when (dest) {
        NavDestination.New -> Icon(Icons.Default.Add, contentDescription = dest.label)
        NavDestination.Collections -> Icon(Icons.Default.List, contentDescription = dest.label)
        NavDestination.History -> Icon(Icons.Default.Search, contentDescription = dest.label)
    }
}

// ── Placeholder screens ───────────────────────────────────────────────────────

@Composable
private fun HistoryPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("History", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}