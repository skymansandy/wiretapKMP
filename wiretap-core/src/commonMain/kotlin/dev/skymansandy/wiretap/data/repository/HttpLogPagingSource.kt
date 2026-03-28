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
import dev.skymansandy.wiretap.domain.model.HttpLogFilter
import dev.skymansandy.wiretap.domain.model.StatusGroup
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
    private val filter: HttpLogFilter,
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
            val statusGroups = filter.statusGroups
            val items = roomDao.getPage(
                query = query,
                noStatusFilter = if (statusGroups.isEmpty()) 1 else 0,
                hasInProgress = if (StatusGroup.InProgress in statusGroups) 1 else 0,
                hasSuccess = if (StatusGroup.Success in statusGroups) 1 else 0,
                hasRedirect = if (StatusGroup.Redirect in statusGroups) 1 else 0,
                hasClientError = if (StatusGroup.ClientError in statusGroups) 1 else 0,
                hasServerError = if (StatusGroup.ServerError in statusGroups) 1 else 0,
                hasFailed = if (StatusGroup.Failed in statusGroups) 1 else 0,
                noMethodFilter = if (filter.methods.isEmpty()) 1 else 0,
                methods = filter.methods.toList().ifEmpty { listOf("") },
                noSourceFilter = if (filter.sources.isEmpty()) 1 else 0,
                sources = filter.sources.map { it.name }.ifEmpty { listOf("") },
                noDomainFilter = if (filter.domains.isEmpty()) 1 else 0,
                domains = filter.domains.toList().ifEmpty { listOf("") },
                afterId = afterId,
                limit = params.loadSize.toLong(),
            ).map {
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
