package dev.skymansandy.kurlclient.ui.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurlclient.db.AppDatabase
import dev.skymansandy.kurlclient.db.CollectionFolder
import dev.skymansandy.kurlclient.db.CollectionRepository
import dev.skymansandy.kurlclient.db.SavedRequest
import kotlinx.coroutines.launch

sealed interface TreeItem {
    data class Folder(val folder: CollectionFolder, val depth: Int, val isExpanded: Boolean) : TreeItem
    data class Request(val request: SavedRequest, val depth: Int) : TreeItem
}

class CollectionsViewModel : ViewModel() {

    private val repo = CollectionRepository(AppDatabase.db)

    var allFolders by mutableStateOf(emptyList<CollectionFolder>())
        private set

    var allRequests by mutableStateOf(emptyList<SavedRequest>())
        private set

    var expandedFolderIds by mutableStateOf(emptySet<Long>())
        private set

    var folderPaths by mutableStateOf(emptyMap<Long, String>())
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            allFolders = repo.getAllFolders()
            allRequests = repo.getAllRequests()
            folderPaths = repo.buildFolderPaths(allFolders)
        }
    }

    fun toggleFolder(id: Long) {
        expandedFolderIds = if (id in expandedFolderIds) expandedFolderIds - id else expandedFolderIds + id
    }

    fun buildTreeItems(): List<TreeItem> {
        val result = mutableListOf<TreeItem>()
        appendChildren(parentId = null, depth = 0, result = result)
        return result
    }

    private fun appendChildren(parentId: Long?, depth: Int, result: MutableList<TreeItem>) {
        allFolders
            .filter { it.parent_id == parentId }
            .sortedBy { it.name }
            .forEach { folder ->
                val expanded = folder.id in expandedFolderIds
                result.add(TreeItem.Folder(folder, depth, expanded))
                if (expanded) appendChildren(folder.id, depth + 1, result)
            }
        allRequests
            .filter { it.folder_id == parentId }
            .sortedBy { it.name }
            .forEach { request ->
                result.add(TreeItem.Request(request, depth))
            }
    }

    fun createFolder(name: String, parentId: Long?) {
        viewModelScope.launch {
            val newId = repo.createFolder(name, parentId)
            refresh()
            if (parentId != null) expandedFolderIds = expandedFolderIds + parentId
            expandedFolderIds = expandedFolderIds + newId
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            repo.deleteFolder(id)
            expandedFolderIds = expandedFolderIds - id
            refresh()
        }
    }

    fun deleteRequest(id: Long) {
        viewModelScope.launch {
            repo.deleteRequest(id)
            refresh()
        }
    }
}