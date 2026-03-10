package dev.skymansandy.wiretap.paging

import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import dev.skymansandy.wiretap.dao.NetworkDao
import dev.skymansandy.wiretap.model.NetworkLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal val defaultPagingConfig = PagingConfig(pageSize = 20, enablePlaceholders = false)

internal class NetworkLogPagingSource(
    private val dao: NetworkDao,
    private val query: String,
    invalidationSignal: SharedFlow<Unit>,
) : PagingSource<Int, NetworkLogEntry>() {

    private val listenerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        listenerScope.launch { invalidationSignal.collect { invalidate() } }
        registerInvalidatedCallback { listenerScope.cancel() }
    }

    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, NetworkLogEntry> {
        val offset = params.key ?: 0
        return try {
            val items = dao.getPage(query, params.loadSize.toLong(), offset.toLong())
            PagingSourceLoadResultPage(
                data = items,
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = if (items.size < params.loadSize) null else offset + params.loadSize,
            )
        } catch (e: Exception) {
            PagingSourceLoadResultError(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NetworkLogEntry>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(state.config.pageSize)
        }
}
