package dev.skymansandy.kurlclient.ui.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.skymansandy.kurlclient.db.CollectionFolder
import dev.skymansandy.kurlclient.db.SavedRequest
import dev.skymansandy.kurlclient.ui.HttpMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    vm: CollectionsViewModel = viewModel { CollectionsViewModel() },
    onRequestSelected: (SavedRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFolderDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Breadcrumb(
                    breadcrumb = vm.breadcrumb,
                    onRootClick = vm::navigateToRoot,
                    onSegmentClick = vm::navigateTo
                )
            },
            navigationIcon = {
                if (vm.breadcrumb.isNotEmpty()) {
                    IconButton(onClick = vm::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up")
                    }
                }
            },
            actions = {
                IconButton(onClick = { showNewFolderDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New folder")
                }
            }
        )
        HorizontalDivider()

        if (vm.folders.isEmpty() && vm.requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No saved requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(vm.folders, key = { "f_${it.id}" }) { folder ->
                    FolderRow(
                        folder = folder,
                        onClick = { vm.navigateInto(folder) },
                        onDelete = { vm.deleteFolder(folder.id) }
                    )
                }
                items(vm.requests, key = { "r_${it.id}" }) { request ->
                    RequestRow(
                        request = request,
                        onClick = { onRequestSelected(request) },
                        onDelete = { vm.deleteRequest(request.id) }
                    )
                }
            }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            allFolders = vm.allFolders,
            folderPaths = vm.folderPaths,
            fixedParentId = vm.currentFolderId,
            onDismiss = { showNewFolderDialog = false },
            onCreate = { name, parentId ->
                vm.createFolder(name, parentId)
                showNewFolderDialog = false
            }
        )
    }
}

// ── Breadcrumb ────────────────────────────────────────────────────────────────

@Composable
private fun Breadcrumb(
    breadcrumb: List<Pair<Long?, String>>,
    onRootClick: () -> Unit,
    onSegmentClick: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onRootClick, modifier = Modifier.padding(0.dp)) {
            Text("Collections", style = MaterialTheme.typography.titleMedium)
        }
        breadcrumb.forEachIndexed { index, (_, name) ->
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (index == breadcrumb.lastIndex) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                TextButton(
                    onClick = { onSegmentClick(index) },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ── Folder row ────────────────────────────────────────────────────────────────

@Composable
private fun FolderRow(
    folder: CollectionFolder,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Folder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            folder.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete folder",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ── Request row ───────────────────────────────────────────────────────────────

@Composable
private fun RequestRow(
    request: SavedRequest,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = methodColor(request.method),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                text = request.method,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(request.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                request.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete request",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

// ── New folder dialog ─────────────────────────────────────────────────────────

@Composable
private fun NewFolderDialog(
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    fixedParentId: Long?,
    onDismiss: () -> Unit,
    onCreate: (name: String, parentId: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var parentId by remember { mutableStateOf<Long?>(fixedParentId) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("New Folder", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (fixedParentId == null) {
                    Column {
                        Text("Parent folder", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = parentId?.let { folderPaths[it] } ?: "No parent (root)",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No parent (root)") },
                                onClick = { parentId = null; dropdownExpanded = false }
                            )
                            allFolders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folderPaths[folder.id] ?: folder.name) },
                                    onClick = { parentId = folder.id; dropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onCreate(name, parentId) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

// ── Method color chip ─────────────────────────────────────────────────────────

@Composable
private fun methodColor(method: String) = when (method) {
    HttpMethod.GET.name -> MaterialTheme.colorScheme.primary
    HttpMethod.POST.name -> MaterialTheme.colorScheme.secondary
    HttpMethod.PUT.name -> MaterialTheme.colorScheme.tertiary
    HttpMethod.DELETE.name -> MaterialTheme.colorScheme.error
    HttpMethod.PATCH.name -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.primary
}