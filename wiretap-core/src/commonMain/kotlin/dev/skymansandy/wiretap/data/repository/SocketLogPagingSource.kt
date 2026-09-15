/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import dev.skymansandy.wiretap.data.db.room.dao.SocketLogsDao
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.model.SocketConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class SocketLogPagingSource(
    private val roomDao: SocketLogsDao,
    private val query: String,
    invalidationSignal: SharedFlow<Unit>,
) : PagingSource<Long, SocketConnection>() {

    private val listenerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        listenerScope.launch {
            invalidationSignal.collect {
                invalidate()
            }
        }

        registerInvalidatedCallback {
            listenerScope.cancel()
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, SocketConnection> {
        val afterId = params.key
        return try {
            val items = roomDao.getSocketLogsPage(query, afterId, params.loadSize.toLong())
                .map { it.toDomain() }
            LoadResult.Page(
                data = items,
                prevKey = null,
                nextKey = if (items.size < params.loadSize) null else items.last().id,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, SocketConnection>): Long? = null
}
