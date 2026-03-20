package dev.skymansandy.wiretap.helper.launcher

import android.Manifest.permission.POST_NOTIFICATIONS
import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.skymansandy.wiretap.data.db.entity.HttpLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketLogEntry
import dev.skymansandy.wiretap.data.db.entity.SocketMessage
import dev.skymansandy.wiretap.domain.model.SocketContentType
import dev.skymansandy.wiretap.domain.model.SocketMessageDirection
import dev.skymansandy.wiretap.domain.model.SocketStatus

internal object WiretapNotificationManager {

    private const val CHANNEL_ID = "wiretap_network_traffic"
    private const val GROUP_KEY = "wiretap_traffic"
    private const val SUMMARY_NOTIFICATION_ID = 9000
    private const val HTTP_NOTIFICATION_ID = 9001
    private const val SOCKET_NOTIFICATION_ID_BASE = 10000
    private const val MAX_ENTRIES = 6
    private const val MAX_SOCKET_MESSAGES = 6

    internal const val ACTION_CLEAR_LOGS = "dev.skymansandy.wiretap.ACTION_CLEAR_LOGS"
    internal const val EXTRA_SOCKET_ID = "wiretap_socket_id"

    private val recentHttpEntries = mutableListOf<HttpLogEntry>()

    // Per-socket recent messages: socketId -> list of formatted message strings
    private val socketMessages = mutableMapOf<Long, MutableList<String>>()

    // Socket entries for status tracking
    private val socketEntries = mutableMapOf<Long, SocketLogEntry>()

    // Track active socket notification IDs for cleanup
    private val activeSocketNotificationIds = mutableSetOf<Int>()

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Network Traffic", IMPORTANCE_LOW).apply {
                description = "Shows recent network requests captured by Wiretap"
            }
            val notifService = context.getSystemService(NotificationManager::class.java)
            notifService?.createNotificationChannel(channel)
        }
    }

    fun onNewEntry(context: Context, entry: HttpLogEntry) {
        if (!hasPermission(context)) return
        val existingIndex = recentHttpEntries.indexOfFirst { it.id == entry.id }
        if (existingIndex >= 0) {
            recentHttpEntries[existingIndex] = entry
        } else {
            if (recentHttpEntries.size >= MAX_ENTRIES) recentHttpEntries.removeAt(0)
            recentHttpEntries.add(entry)
        }
        postHttpNotification(context)
        postSummaryIfNeeded(context)
    }

    fun onNewSocketEntry(context: Context, entry: SocketLogEntry) {
        if (!hasPermission(context)) return
        socketEntries[entry.id] = entry

        // Update socket message notification with latest status (ongoing/closed)
        val messages = socketMessages[entry.id]
        if (messages != null) {
            postSocketMessageNotification(context, entry, messages)
        }
        postSummaryIfNeeded(context)
    }

    fun onNewSocketMessage(context: Context, entry: SocketLogEntry, message: SocketMessage) {
        if (!hasPermission(context)) return
        socketEntries[entry.id] = entry
        val messages = socketMessages.getOrPut(entry.id) { mutableListOf() }
        if (messages.size >= MAX_SOCKET_MESSAGES) messages.removeAt(0)
        messages.add(formatSocketMessage(message))
        postSocketMessageNotification(context, entry, messages)
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

    fun clearSocketNotifications(context: Context) {
        socketMessages.clear()
        socketEntries.clear()
        val manager = NotificationManagerCompat.from(context)
        activeSocketNotificationIds.forEach { manager.cancel(it) }
        activeSocketNotificationIds.clear()
        // Update or cancel summary
        if (recentHttpEntries.isEmpty()) {
            manager.cancel(SUMMARY_NOTIFICATION_ID)
        }
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun formatHttpEntry(entry: HttpLogEntry): String {
        val status = when {
            entry.responseCode == HttpLogEntry.RESPONSE_CODE_IN_PROGRESS -> "..."
            entry.responseCode > 0 -> entry.responseCode.toString()
            entry.responseCode == -1 -> "!!!"
            else -> "ERR"
        }
        return "${entry.method}  $status  ${entry.url}"
    }

    private fun formatSocketMessage(message: SocketMessage): String {
        val direction = if (message.direction == SocketMessageDirection.Sent) "▲" else "▼"
        val content = if (message.contentType == SocketContentType.Binary) {
            "[Binary: ${message.byteCount} B]"
        } else {
            message.content.take(100)
        }
        return "$direction $content"
    }

    private fun socketUrlDisplay(url: String): String {
        val afterScheme = url.substringAfter("://")
        val host = afterScheme.substringBefore("/").substringBefore("?")
        val path = afterScheme.removePrefix(host).ifEmpty { "/" }
        return "$host$path"
    }

    private fun socketNotificationId(socketId: Long): Int =
        SOCKET_NOTIFICATION_ID_BASE + (socketId % 1000).toInt()

    /**
     * Posts the group summary. Only needed when there are multiple child notifications
     * (HTTP + at least one socket). Without children, the HTTP notification shows standalone.
     */
    @SuppressLint("MissingPermission")
    private fun postSummaryIfNeeded(context: Context) {
        if (!hasPermission(context)) return
        if (activeSocketNotificationIds.isEmpty() || recentHttpEntries.isEmpty()) return

        val total = recentHttpEntries.size + socketEntries.size
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
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
        if (!hasPermission(context)) return
        if (recentHttpEntries.isEmpty()) return

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("View network traffic")
        recentHttpEntries.forEach { entry ->
            inboxStyle.addLine(formatHttpEntry(entry))
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("View network traffic")
            .setContentText(formatHttpEntry(recentHttpEntries.last()))
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
        entry: SocketLogEntry,
        messages: List<String>,
    ) {
        if (!hasPermission(context)) return

        val notificationId = socketNotificationId(entry.id)
        activeSocketNotificationIds.add(notificationId)

        val urlDisplay = socketUrlDisplay(entry.url)
        val statusLabel = entry.status.name
        val title = "WS $urlDisplay [$statusLabel]"

        val isActive = entry.status == SocketStatus.Open || entry.status == SocketStatus.Connecting

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        messages.forEach { inboxStyle.addLine(it) }
        if (entry.messageCount > messages.size) {
            inboxStyle.setSummaryText("${entry.messageCount} messages total")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messages.lastOrNull() ?: "No messages")
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setOngoing(isActive)
            .setGroup(GROUP_KEY)
            .setContentIntent(openSocketDetailIntent(context, entry.id))
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun openWiretapIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            getLaunchIntent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun openSocketDetailIntent(context: Context, socketId: Long): PendingIntent =
        PendingIntent.getActivity(
            context,
            socketId.toInt(),
            getLaunchIntent().apply {
                putExtra(EXTRA_SOCKET_ID, socketId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun clearLogsIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_CLEAR_LOGS).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
