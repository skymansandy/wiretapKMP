package dev.skymansandy.wiretap.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dev.skymansandy.wiretap.WiretapActivity
import dev.skymansandy.wiretap.model.NetworkLogEntry

internal object WiretapNotificationManager {

    private const val CHANNEL_ID = "wiretap_network_traffic"
    private const val NOTIFICATION_ID = 9000
    private const val MAX_ENTRIES = 6

    internal const val ACTION_CLEAR_LOGS = "dev.skymansandy.wiretap.ACTION_CLEAR_LOGS"

    private val recentEntries = ArrayDeque<NetworkLogEntry>(MAX_ENTRIES)

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Traffic",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows recent network requests captured by Wiretap"
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    fun onNewEntry(context: Context, entry: NetworkLogEntry) {
        if (!hasPermission(context)) return
        if (recentEntries.size >= MAX_ENTRIES) recentEntries.removeFirst()
        recentEntries.addLast(entry)
        postNotifications(context)
    }

    fun clearAll(context: Context) {
        recentEntries.clear()
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    private fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun postNotifications(context: Context) {
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("View network traffic")
        recentEntries.forEach { entry ->
            inboxStyle.addLine("${entry.method}  ${entry.responseCode}  ${entry.url}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("View network traffic")
            .setContentText(recentEntries.last().let { "${it.method}  ${it.responseCode}  ${it.url}" })
            .setStyle(inboxStyle)
            .setOnlyAlertOnce(true)
            .setContentIntent(openWiretapIntent(context))
            .addAction(0, "Clear logs", clearLogsIntent(context))
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun openWiretapIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, WiretapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
