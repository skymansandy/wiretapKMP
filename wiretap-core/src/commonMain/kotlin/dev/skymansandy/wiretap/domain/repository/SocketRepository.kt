/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.repository

import app.cash.paging.PagingData
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import kotlinx.coroutines.flow.Flow

interface SocketRepository {

    fun flowById(id: Long): Flow<SocketConnection?>

    fun flowMessagesForId(socketId: Long): Flow<List<SocketMessage>>

    fun flowAll(): Flow<List<SocketConnection>>

    fun flowForSearchQuery(query: String): Flow<PagingData<SocketConnection>>

    suspend fun logNew(socket: SocketConnection): Long

    suspend fun markReopened(socket: SocketConnection)

    suspend fun update(socket: SocketConnection)

    suspend fun logSocketMsg(message: SocketMessage)

    suspend fun getById(id: Long): SocketConnection?

    suspend fun clearAll()

    suspend fun clearClosed()
}
