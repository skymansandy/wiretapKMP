package dev.skymansandy.wiretap.data.repository

import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultError
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.mappers.toDomain
import dev.skymansandy.wiretap.domain.model.HttpLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20
internal val defaultPagingConfig = PagingConfig(
    pageSize = PAGE_SIZE,
    prefetchDistance = 3 * PAGE_SIZE,
    enablePlaceholders = false,
)

internal class HttpLogPagingSource(
    private val roomDao: HttpLogsDao,
    private val query: String,
    invalidationSignal: SharedFlow<Unit>,
) : PagingSource<Long, HttpLog>() {

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

    override suspend fun load(params: PagingSourceLoadParams<Long>): PagingSourceLoadResult<Long, HttpLog> {
        val afterId = params.key
        return try {
            val items = roomDao.getPage(query, afterId, params.loadSize.toLong()).map {
                it.toDomain()
            }

            PagingSourceLoadResultPage(
                data = items,
                prevKey = null, // newest-first; no back-paging needed
                nextKey = if (items.size < params.loadSize) null else items.last().id,
            )
        } catch (e: Exception) {
            PagingSourceLoadResultError(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, HttpLog>): Long? = null
}
