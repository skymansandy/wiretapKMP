package dev.skymansandy.kurlclient.ui.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.skymansandy.kurlclient.db.CollectionFolder

@Composable
fun SaveRequestDialog(
    initialName: String,
    folders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    onSave: (name: String, folderId: Long?) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    var showNewFolderRow by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderParentDropdownExpanded by remember { mutableStateOf(false) }
    var newFolderParentId by remember { mutableStateOf<Long?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Save Request", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Request name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Folder picker
                Column {
                    Text("Save in folder", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { folderDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedFolderId?.let { folderPaths[it] } ?: "No folder (root)",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = folderDropdownExpanded,
                        onDismissRequest = { folderDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No folder (root)") },
                            onClick = { selectedFolderId = null; folderDropdownExpanded = false }
                        )
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folderPaths[folder.id] ?: folder.name) },
                                onClick = { selectedFolderId = folder.id; folderDropdownExpanded = false }
                            )
                        }
                    }
                }

                // New folder section
                if (showNewFolderRow) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("New folder", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text("Folder name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Parent folder for new folder
                        OutlinedButton(
                            onClick = { newFolderParentDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = newFolderParentId?.let { folderPaths[it] } ?: "No parent (root)",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = newFolderParentDropdownExpanded,
                            onDismissRequest = { newFolderParentDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No parent (root)") },
                                onClick = { newFolderParentId = null; newFolderParentDropdownExpanded = false }
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folderPaths[folder.id] ?: folder.name) },
                                    onClick = { newFolderParentId = folder.id; newFolderParentDropdownExpanded = false }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showNewFolderRow = false; newFolderName = "" }) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        onCreateFolder(newFolderName, newFolderParentId)
                                        showNewFolderRow = false
                                        newFolderName = ""
                                    }
                                },
                                enabled = newFolderName.isNotBlank()
                            ) {
                                Text("Create")
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { showNewFolderRow = true }) {
                        Text("+ New folder")
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, selectedFolderId) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}