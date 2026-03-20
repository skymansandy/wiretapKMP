package dev.skymansandy.wiretap.data.repository

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class SocketLogPagingSource(
    private val dao: SocketDao,
    private val query: String,
    invalidationSignal: SharedFlow<Unit>,
) : PagingSource<Long, SocketLogEntry>() {

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

    override suspend fun load(params: PagingSourceLoadParams<Long>): PagingSourceLoadResult<Long, SocketLogEntry> {
        val afterId = params.key
        return try {
            val items = dao.getPage(query, params.loadSize.toLong(), afterId)
            PagingSourceLoadResultPage(
                data = items,
                prevKey = null,
                nextKey = if (items.size < params.loadSize) null else items.last().id,
            )
        } catch (e: Exception) {
            PagingSourceLoadResultError(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, SocketLogEntry>): Long? = null
}
