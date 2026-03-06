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

class CollectionsViewModel : ViewModel() {

    private val repo = CollectionRepository(AppDatabase.db)

    // Breadcrumb stack: list of (folderId, folderName) pairs; null id = root
    var breadcrumb by mutableStateOf(listOf<Pair<Long?, String>>())
        private set

    val currentFolderId: Long? get() = breadcrumb.lastOrNull()?.first

    var folders by mutableStateOf(emptyList<CollectionFolder>())
        private set

    var requests by mutableStateOf(emptyList<SavedRequest>())
        private set

    var allFolders by mutableStateOf(emptyList<CollectionFolder>())
        private set

    var folderPaths by mutableStateOf(emptyMap<Long, String>())
        private set

    init {
        loadRoot()
    }

    fun refresh() {
        loadCurrent()
    }

    private fun loadRoot() {
        breadcrumb = emptyList()
        loadCurrent()
    }

    private fun loadCurrent() {
        viewModelScope.launch {
            val folderId = currentFolderId
            folders = repo.getFoldersInParent(folderId)
            requests = repo.getRequestsInFolder(folderId)
            allFolders = repo.getAllFolders()
            folderPaths = repo.buildFolderPaths(allFolders)
        }
    }

    fun navigateInto(folder: CollectionFolder) {
        breadcrumb = breadcrumb + (folder.id to folder.name)
        loadCurrent()
    }

    fun navigateUp() {
        if (breadcrumb.isNotEmpty()) {
            breadcrumb = breadcrumb.dropLast(1)
            loadCurrent()
        }
    }

    fun navigateTo(index: Int) {
        breadcrumb = breadcrumb.take(index + 1)
        loadCurrent()
    }

    fun navigateToRoot() {
        breadcrumb = emptyList()
        loadCurrent()
    }

    fun createFolder(name: String, parentId: Long?) {
        viewModelScope.launch {
            repo.createFolder(name, parentId)
            loadCurrent()
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            repo.deleteFolder(id)
            loadCurrent()
        }
    }

    fun deleteRequest(id: Long) {
        viewModelScope.launch {
            repo.deleteRequest(id)
            loadCurrent()
        }
    }
}