package dev.skymansandy.kurlclient.ui.collections

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
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
import dev.skymansandy.kurlclient.formatRelativeTime
import dev.skymansandy.kurlclient.ui.HttpMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    vm: CollectionsViewModel = viewModel { CollectionsViewModel() },
    activeRequestId: Long? = null,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderParentId by remember { mutableStateOf<Long?>(null) }

    val treeItems = remember(vm.allFolders, vm.allRequests, vm.expandedFolderIds) {
        vm.buildTreeItems()
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text("Collections", style = MaterialTheme.typography.titleMedium) },
            actions = {
                IconButton(onClick = {
                    newFolderParentId = null
                    showNewFolderDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "New folder")
                }
            }
        )
        HorizontalDivider()

        if (treeItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No saved requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(treeItems, key = { item ->
                    when (item) {
                        is TreeItem.Folder -> "f_${item.folder.id}"
                        is TreeItem.Request -> "r_${item.request.id}"
                    }
                }) { item ->
                    when (item) {
                        is TreeItem.Folder -> FolderTreeRow(
                            item = item,
                            onToggle = { vm.toggleFolder(item.folder.id) },
                            onNewSubfolder = {
                                newFolderParentId = item.folder.id
                                showNewFolderDialog = true
                            },
                            onDelete = { vm.deleteFolder(item.folder.id) }
                        )
                        is TreeItem.Request -> RequestTreeRow(
                            item = item,
                            isActive = item.request.id == activeRequestId,
                            onLoad = { onRequestSelected(item.request) },
                            onSaveChanges = onSaveChanges,
                            onDelete = { vm.deleteRequest(item.request.id) }
                        )
                    }
                }
            }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            allFolders = vm.allFolders,
            folderPaths = vm.folderPaths,
            fixedParentId = newFolderParentId,
            onDismiss = { showNewFolderDialog = false },
            onCreate = { name, parentId ->
                vm.createFolder(name, parentId)
                showNewFolderDialog = false
            }
        )
    }
}

// ── Folder tree row ───────────────────────────────────────────────────────────

@Composable
private fun FolderTreeRow(
    item: TreeItem.Folder,
    onToggle: () -> Unit,
    onNewSubfolder: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val indent = (item.depth * 16).dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 8.dp + indent, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (item.isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
            contentDescription = if (item.isExpanded) "Collapse" else "Expand",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            imageVector = if (item.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = item.folder.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Folder options",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("New subfolder") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    onClick = { menuExpanded = false; onNewSubfolder() }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { menuExpanded = false; onDelete() }
                )
            }
        }
    }
}

// ── Request tree row ──────────────────────────────────────────────────────────

@Composable
private fun RequestTreeRow(
    item: TreeItem.Request,
    isActive: Boolean,
    onLoad: () -> Unit,
    onSaveChanges: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val indent = (item.depth * 16).dp
    val request = item.request

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                else Modifier
            )
            .clickable(onClick = onLoad)
            .padding(start = 32.dp + indent, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = methodColor(request.method),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                text = request.method,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = request.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = formatRelativeTime(request.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = request.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Request options",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Load") },
                    onClick = { menuExpanded = false; onLoad() }
                )
                if (isActive) {
                    DropdownMenuItem(
                        text = { Text("Save changes") },
                        onClick = { menuExpanded = false; onSaveChanges() }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { menuExpanded = false; onDelete() }
                )
            }
        }
    }
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
                        Text(
                            "Parent folder",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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