package dev.skymansandy.kurlclient.db

import dev.skymansandy.kurlclient.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CollectionRepository(private val db: KurlDatabase) {

    // ── Folders ───────────────────────────────────────────────────────────────

    suspend fun createFolder(name: String, parentId: Long?): Long = withContext(Dispatchers.Default) {
        db.collectionsQueries.insertFolder(name = name, parent_id = parentId, created_at = currentTimeMillis())
        db.collectionsQueries.lastInsertedRowId().executeAsOne()
    }

    suspend fun getFoldersInParent(parentId: Long?): List<CollectionFolder> =
        withContext(Dispatchers.Default) {
            db.collectionsQueries.getFoldersByParent(parentId).executeAsList()
        }

    suspend fun getAllFolders(): List<CollectionFolder> = withContext(Dispatchers.Default) {
        db.collectionsQueries.getAllFolders().executeAsList()
    }

    suspend fun deleteFolder(id: Long) = withContext(Dispatchers.Default) {
        db.collectionsQueries.deleteFolder(id)
    }

    // ── Requests ──────────────────────────────────────────────────────────────

    suspend fun saveRequest(
        name: String,
        folderId: Long?,
        url: String,
        method: String,
        headers: String,
        params: String,
        body: String
    ) = withContext(Dispatchers.Default) {
        db.collectionsQueries.insertRequest(
            name = name,
            folder_id = folderId,
            url = url,
            method = method,
            headers = headers,
            params = params,
            body = body,
            created_at = currentTimeMillis()
        )
    }

    suspend fun updateRequest(
        id: Long,
        name: String,
        folderId: Long?,
        url: String,
        method: String,
        headers: String,
        params: String,
        body: String
    ) = withContext(Dispatchers.Default) {
        db.collectionsQueries.updateRequest(
            id = id,
            name = name,
            folder_id = folderId,
            url = url,
            method = method,
            headers = headers,
            params = params,
            body = body,
            created_at = currentTimeMillis()
        )
    }

    suspend fun getAllRequests(): List<SavedRequest> = withContext(Dispatchers.Default) {
        db.collectionsQueries.getAllRequests().executeAsList()
    }

    suspend fun getRequestsInFolder(folderId: Long?): List<SavedRequest> =
        withContext(Dispatchers.Default) {
            db.collectionsQueries.getRequestsByFolder(folderId).executeAsList()
        }

    suspend fun deleteRequest(id: Long) = withContext(Dispatchers.Default) {
        db.collectionsQueries.deleteRequest(id)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a map from folder id to its full display path (e.g. "Parent / Child"). */
    suspend fun buildFolderPaths(folders: List<CollectionFolder>): Map<Long, String> =
        withContext(Dispatchers.Default) {
            val folderMap = folders.associateBy { it.id }
            folders.associate { folder ->
                var path = folder.name
                var parentId = folder.parent_id
                while (parentId != null) {
                    val parent = folderMap[parentId] ?: break
                    path = "${parent.name} / $path"
                    parentId = parent.parent_id
                }
                folder.id to path
            }
        }
}