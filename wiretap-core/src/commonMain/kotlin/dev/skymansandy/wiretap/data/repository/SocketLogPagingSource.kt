package dev.skymansandy.wiretap.data.repository

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import dev.skymansandy.wiretap.data.db.entity.SocketEntry
import dev.skymansandy.wiretap.data.db.room.dao.SocketRoomDao
import dev.skymansandy.wiretap.data.mappers.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class SocketLogPagingSource(
    private val roomDao: SocketRoomDao,
    private val query: String,
    invalidationSignal: SharedFlow<Unit>,
) : PagingSource<Long, SocketEntry>() {

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

    override suspend fun load(params: PagingSourceLoadParams<Long>): PagingSourceLoadResult<Long, SocketEntry> {
        val afterId = params.key
        return try {
            val items = roomDao.getSocketLogsPage(query, afterId, params.loadSize.toLong())
                .map { it.toDomain() }
            PagingSourceLoadResultPage(
                data = items,
                prevKey = null,
                nextKey = if (items.size < params.loadSize) null else items.last().id,
            )
        } catch (e: Exception) {
            PagingSourceLoadResultError(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, SocketEntry>): Long? = null
}
