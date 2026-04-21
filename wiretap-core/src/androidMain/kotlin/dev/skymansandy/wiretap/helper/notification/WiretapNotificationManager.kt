/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.domain.model.SseConnection
import dev.skymansandy.wiretap.domain.model.SseEvent
import dev.skymansandy.wiretap.domain.model.SseStatus
import dev.skymansandy.wiretap.helper.launcher.WiretapIconFactory
import dev.skymansandy.wiretap.helper.launcher.getLaunchIntent
import dev.skymansandy.wiretap.helper.util.PermissionUtil.canPostNotifications

internal object WiretapNotificationManager {

    private const val CHANNEL_ID = "wiretap_network_traffic"
    private const val GROUP_KEY = "wiretap_traffic"
    private const val SUMMARY_NOTIFICATION_ID = 9000
    private const val HTTP_NOTIFICATION_ID = 9001
    private const val SOCKET_NOTIFICATION_ID_BASE = 10000
    private const val SSE_NOTIFICATION_ID_BASE = 20000
    private const val MAX_ENTRIES = 6
    private const val MAX_SOCKET_MESSAGES = 6
    private const val MAX_SSE_EVENTS = 6

    internal const val ACTION_CLEAR_HTTP_LOGS = "dev.skymansandy.wiretap.ACTION_CLEAR_HTTP_LOGS"
    internal const val EXTRA_SOCKET_ID = "wiretap_socket_id"
    internal const val EXTRA_SSE_CONNECTION_ID = "wiretap_sse_connection_id"

    private val notificationIcon by lazy {
        IconCompat.createWithBitmap(WiretapIconFactory.notificationBitmap)
    }

    private val lock = Any()

    private val recentHttpEntries = mutableListOf<HttpLog>()

    private val socketMessages = mutableMapOf<Long, MutableList<String>>()
    private val socketEntries = mutableMapOf<Long, SocketConnection>()
    private val activeSocketNotificationIds = mutableSetOf<Int>()

    private val sseEvents = mutableMapOf<Long, MutableList<String>>()
    private val sseEntries = mutableMapOf<Long, SseConnection>()
    private val activeSseNotificationIds = mutableSetOf<Int>()

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Network Traffic", IMPORTANCE_LOW).apply {
                description = "Shows recent network requests captured by Wiretap"
            }

            val notifService = context.getSystemService(NotificationManager::class.java)
            notifService?.createNotificationChannel(channel)
        }
    }

    fun notifyHttpLog(context: Context, log: HttpLog) {
        if (!canPostNotifications(context)) return

        synchronized(lock) {
            val existingIndex = recentHttpEntries.indexOfFirst { it.id == log.id }
            if (existingIndex >= 0) {
                recentHttpEntries[existingIndex] = log
            } else {
                if (recentHttpEntries.size >= MAX_ENTRIES) recentHttpEntries.removeAt(0)
                recentHttpEntries.add(log)
            }
        }

        postHttpNotification(context)
        postSummaryIfNeeded(context)
    }

    fun notifyNewSocket(context: Context, entry: SocketConnection) {
        if (!canPostNotifications(context)) return

        val snapshot: List<String>
        synchronized(lock) {
            socketEntries[entry.id] = entry
            snapshot = socketMessages[entry.id]?.toList() ?: emptyList()
        }
        postSocketMessageNotification(context, entry, snapshot)

        postSummaryIfNeeded(context)
    }

    fun notifySocketMessage(context: Context, entry: SocketConnection, message: SocketMessage) {
        if (!canPostNotifications(context)) return

        val snapshot: List<String>
        synchronized(lock) {
            socketEntries[entry.id] = entry
            val messages = socketMessages.getOrPut(entry.id) { mutableListOf() }
            if (messages.size >= MAX_SOCKET_MESSAGES) messages.removeAt(0)
            messages.add(NotificationFormatUtil.formatSocketMessage(message))
            snapshot = messages.toList()
        }
        postSocketMessageNotification(context, entry, snapshot)

        postSummaryIfNeeded(context)
    }

    fun removeHttpEntry(context: Context, id: Long) {
        val removed = synchronized(lock) {
            val index = recentHttpEntries.indexOfFirst { it.id == id }
            if (index >= 0) {
                recentHttpEntries.removeAt(index)
                true
            } else {
                false
            }
        }
        if (removed) {
            val snapshot = synchronized(lock) { recentHttpEntries.toList() }
            if (snapshot.isEmpty()) {
                NotificationManagerCompat.from(context).cancel(HTTP_NOTIFICATION_ID)
            } else {
                postHttpNotification(context)
            }
        }
    }

    fun clearHttpNotifications(context: Context) {
        synchronized(lock) {
            recentHttpEntries.clear()
        }
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(HTTP_NOTIFICATION_ID)

        val cancelSummary = synchronized(lock) {
            activeSocketNotificationIds.isEmpty() && activeSseNotificationIds.isEmpty()
        }
        if (cancelSummary) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    fun clearSockets(context: Context) {
        val idsToCancel: List<Int>
        synchronized(lock) {
            socketMessages.clear()
            socketEntries.clear()
            idsToCancel = activeSocketNotificationIds.toList()
            activeSocketNotificationIds.clear()
        }

        val manager = NotificationManagerCompat.from(context)
        idsToCancel.forEach { manager.cancel(it) }

        val cancelSummary = synchronized(lock) {
            recentHttpEntries.isEmpty() && activeSseNotificationIds.isEmpty()
        }
        if (cancelSummary) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    fun notifyNewSse(context: Context, entry: SseConnection) {
        if (!canPostNotifications(context)) return

        val snapshot: List<String>
        synchronized(lock) {
            sseEntries[entry.id] = entry
            snapshot = sseEvents[entry.id]?.toList() ?: emptyList()
        }
        postSseEventNotification(context, entry, snapshot)

        postSummaryIfNeeded(context)
    }

    fun notifySseEvent(context: Context, entry: SseConnection, event: SseEvent) {
        if (!canPostNotifications(context)) return

        val snapshot: List<String>
        synchronized(lock) {
            sseEntries[entry.id] = entry
            val events = sseEvents.getOrPut(entry.id) { mutableListOf() }
            if (events.size >= MAX_SSE_EVENTS) events.removeAt(0)
            events.add(NotificationFormatUtil.formatSseEvent(event))
            snapshot = events.toList()
        }
        postSseEventNotification(context, entry, snapshot)

        postSummaryIfNeeded(context)
    }

    fun clearSse(context: Context) {
        val idsToCancel: List<Int>
        synchronized(lock) {
            sseEvents.clear()
            sseEntries.clear()
            idsToCancel = activeSseNotificationIds.toList()
            activeSseNotificationIds.clear()
        }

        val manager = NotificationManagerCompat.from(context)
        idsToCancel.forEach { manager.cancel(it) }

        val cancelSummary = synchronized(lock) {
            recentHttpEntries.isEmpty() && activeSocketNotificationIds.isEmpty()
        }
        if (cancelSummary) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    private fun socketNotificationId(socketId: Long): Int =
        SOCKET_NOTIFICATION_ID_BASE + (socketId % 1000).toInt()

    private fun sseNotificationId(connectionId: Long): Int =
        SSE_NOTIFICATION_ID_BASE + (connectionId % 1000).toInt()

    @SuppressLint("MissingPermission")
    private fun postSummaryIfNeeded(context: Context) {
        if (!canPostNotifications(context)) return
        val (hasMultipleGroups, total) = synchronized(lock) {
            val groups = listOf(
                recentHttpEntries.isNotEmpty(),
                activeSocketNotificationIds.isNotEmpty(),
                activeSseNotificationIds.isNotEmpty(),
            ).count { it }
            val total = recentHttpEntries.size + socketEntries.size + sseEntries.size
            (groups >= 2) to total
        }
        if (!hasMultipleGroups) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle("Wiretap")
            .setContentText("$total active connections")
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setContentIntent(openWiretapIntent(context))
            .addAction(0, "Clear logs", clearLogsIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(SUMMARY_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private fun postHttpNotification(context: Context) {
        if (!canPostNotifications(context)) return

        val snapshot = synchronized(lock) { recentHttpEntries.toList() }
        if (snapshot.isEmpty()) return

        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle("View network traffic")
        snapshot.forEach { entry ->
            inboxStyle.addLine(NotificationFormatUtil.formatHttpEntry(entry))
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle("View network traffic")
            .setContentText(NotificationFormatUtil.formatHttpEntry(snapshot.last()))
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY)
            .setContentIntent(openWiretapIntent(context))
            .addAction(0, "Clear logs", clearLogsIntent(context))

        NotificationManagerCompat.from(context).notify(HTTP_NOTIFICATION_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    private fun postSocketMessageNotification(
        context: Context,
        socket: SocketConnection,
        messages: List<String>,
    ) {
        if (!canPostNotifications(context)) return

        val notificationId = socketNotificationId(socket.id)
        synchronized(lock) { activeSocketNotificationIds.add(notificationId) }

        val urlDisplay = NotificationFormatUtil.socketUrlDisplay(socket.url)
        val statusLabel = socket.status.name
        val title = "WS $urlDisplay [$statusLabel]"

        val isActive =
            socket.status == SocketStatus.Open || socket.status == SocketStatus.Connecting

        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
        messages.forEach { inboxStyle.addLine(it) }

        if (socket.messageCount > messages.size) {
            inboxStyle.setSummaryText("${socket.messageCount} messages total")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(messages.lastOrNull() ?: "No messages")
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setOngoing(isActive)
            .setGroup(GROUP_KEY)
            .setContentIntent(openSocketDetailIntent(context, socket.id))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun postSseEventNotification(
        context: Context,
        connection: SseConnection,
        events: List<String>,
    ) {
        if (!canPostNotifications(context)) return

        val notificationId = sseNotificationId(connection.id)
        synchronized(lock) { activeSseNotificationIds.add(notificationId) }

        val urlDisplay = NotificationFormatUtil.sseUrlDisplay(connection.url)
        val statusLabel = connection.status.name
        val title = "SSE $urlDisplay [$statusLabel]"

        val isActive =
            connection.status == SseStatus.Open || connection.status == SseStatus.Connecting

        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
        events.forEach { inboxStyle.addLine(it) }

        if (connection.eventCount > events.size) {
            inboxStyle.setSummaryText("${connection.eventCount} events total")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(events.lastOrNull() ?: "No events")
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setOngoing(isActive)
            .setGroup(GROUP_KEY)
            .setContentIntent(openSseDetailIntent(context, connection.id))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun openWiretapIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ getLaunchIntent(),
            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun openSocketDetailIntent(context: Context, socketId: Long): PendingIntent =
        PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ SOCKET_NOTIFICATION_ID_BASE + socketId.toInt(),
            /* intent = */
            getLaunchIntent().apply {
                putExtra(EXTRA_SOCKET_ID, socketId)
                removeExtra(EXTRA_SSE_CONNECTION_ID)
            },
            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun openSseDetailIntent(context: Context, connectionId: Long): PendingIntent =
        PendingIntent.getActivity(
            /* context = */ context,
            /* requestCode = */ SSE_NOTIFICATION_ID_BASE + connectionId.toInt(),
            /* intent = */
            getLaunchIntent().apply {
                putExtra(EXTRA_SSE_CONNECTION_ID, connectionId)
                removeExtra(EXTRA_SOCKET_ID)
            },
            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun clearLogsIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ 0,
            /* intent = */ Intent(ACTION_CLEAR_HTTP_LOGS).setPackage(context.packageName),
            /* flags = */ PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
