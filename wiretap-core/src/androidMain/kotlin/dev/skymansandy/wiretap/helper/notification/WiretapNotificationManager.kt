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
import co.touchlab.stately.collections.ConcurrentMutableList
import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.SocketConnection
import dev.skymansandy.wiretap.domain.model.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketStatus
import dev.skymansandy.wiretap.helper.launcher.WiretapIconFactory
import dev.skymansandy.wiretap.helper.launcher.getLaunchIntent
import dev.skymansandy.wiretap.helper.util.PermissionUtil.canPostNotifications

internal object WiretapNotificationManager {

    private const val CHANNEL_ID = "wiretap_network_traffic"
    private const val GROUP_KEY = "wiretap_traffic"
    private const val SUMMARY_NOTIFICATION_ID = 9000
    private const val HTTP_NOTIFICATION_ID = 9001
    private const val SOCKET_NOTIFICATION_ID_BASE = 10000
    private const val MAX_ENTRIES = 6
    private const val MAX_SOCKET_MESSAGES = 6

    internal const val ACTION_CLEAR_HTTP_LOGS = "dev.skymansandy.wiretap.ACTION_CLEAR_HTTP_LOGS"
    internal const val EXTRA_SOCKET_ID = "wiretap_socket_id"

    private val notificationIcon by lazy {
        IconCompat.createWithBitmap(WiretapIconFactory.notificationBitmap)
    }

    private val recentHttpEntries = ConcurrentMutableList<HttpLog>()

    // Per-socket recent messages: socketId -> list of formatted message strings
    private val socketMessages = ConcurrentMutableMap<Long, ConcurrentMutableList<String>>()

    // Socket entries for status tracking
    private val socketEntries = ConcurrentMutableMap<Long, SocketConnection>()

    // Track active socket notification IDs for cleanup
    private val activeSocketNotificationIds = ConcurrentMutableSet<Int>()

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

        // Snapshot to avoid ConcurrentModificationException during indexOfFirst iteration
        val existingIndex = recentHttpEntries.toList().indexOfFirst { it.id == log.id }
        if (existingIndex >= 0) {
            recentHttpEntries[existingIndex] = log
        } else {
            if (recentHttpEntries.size >= MAX_ENTRIES) recentHttpEntries.removeAt(0)
            recentHttpEntries.add(log)
        }

        postHttpNotification(context)

        postSummaryIfNeeded(context)
    }

    fun notifyNewSocket(context: Context, entry: SocketConnection) {
        if (!canPostNotifications(context)) return

        // Update socket message notification with latest status (ongoing/closed)
        socketEntries[entry.id] = entry
        val messages = socketMessages[entry.id]
        if (messages != null) {
            postSocketMessageNotification(context, entry, messages.toList())
        }

        postSummaryIfNeeded(context)
    }

    fun notifySocketMessage(context: Context, entry: SocketConnection, message: SocketMessage) {
        if (!canPostNotifications(context)) return

        socketEntries[entry.id] = entry
        val messages = socketMessages.getOrPut(entry.id) { ConcurrentMutableList() }
        if (messages.size >= MAX_SOCKET_MESSAGES) messages.removeAt(0)
        messages.add(NotificationFormatUtil.formatSocketMessage(message))
        postSocketMessageNotification(context, entry, messages.toList())

        postSummaryIfNeeded(context)
    }

    fun clearHttpNotifications(context: Context) {
        recentHttpEntries.clear()
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(HTTP_NOTIFICATION_ID)

        // Update or cancel summary
        if (activeSocketNotificationIds.isEmpty()) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    fun clearSockets(context: Context) {
        socketMessages.clear()
        socketEntries.clear()

        val manager = NotificationManagerCompat.from(context)
        // Snapshot to avoid ConcurrentModificationException during forEach
        activeSocketNotificationIds.toList().forEach { manager.cancel(it) }
        activeSocketNotificationIds.clear()

        // Update or cancel summary
        if (recentHttpEntries.isEmpty()) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    private fun socketNotificationId(socketId: Long): Int =
        SOCKET_NOTIFICATION_ID_BASE + (socketId % 1000).toInt()

    /**
     * Posts the group summary. Only needed when there are multiple child notifications
     * (HTTP + at least one socket). Without children, the HTTP notification shows standalone.
     */
    @SuppressLint("MissingPermission")
    private fun postSummaryIfNeeded(context: Context) {
        if (!canPostNotifications(context)) return
        if (activeSocketNotificationIds.isEmpty() || recentHttpEntries.isEmpty()) return

        val total = recentHttpEntries.size + socketEntries.size
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

    /**
     * Single notification for all HTTP traffic, shown as InboxStyle with recent entries.
     * When socket notifications exist, this becomes a child in the group.
     */
    @SuppressLint("MissingPermission")
    private fun postHttpNotification(context: Context) {
        if (!canPostNotifications(context)) return

        // Snapshot to avoid ConcurrentModificationException during forEach
        val snapshot = recentHttpEntries.toList()
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
        activeSocketNotificationIds.add(notificationId)

        val urlDisplay = NotificationFormatUtil.socketUrlDisplay(socket.url)
        val statusLabel = socket.status.name
        val title = "WS $urlDisplay [$statusLabel]"

        val isActive =
            socket.status == SocketStatus.Open || socket.status == SocketStatus.Connecting

        val messageSnapshot = messages.toList()
        val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
        messageSnapshot.forEach { inboxStyle.addLine(it) }

        if (socket.messageCount > messageSnapshot.size) {
            inboxStyle.setSummaryText("${socket.messageCount} messages total")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(messageSnapshot.lastOrNull() ?: "No messages")
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setOngoing(isActive)
            .setGroup(GROUP_KEY)
            .setContentIntent(openSocketDetailIntent(context, socket.id))
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
            /* requestCode = */ socketId.toInt(),
            /* intent = */
            getLaunchIntent().apply {
                putExtra(EXTRA_SOCKET_ID, socketId)
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
