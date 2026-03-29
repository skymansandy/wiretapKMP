/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.skymansandy.wiretap.di.WiretapKoinContext
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager.ACTION_CLEAR_HTTP_LOGS
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class WiretapClearLogsReceiver : BroadcastReceiver(), KoinComponent {

    override fun getKoin(): Koin = WiretapKoinContext.koin

    private val httpLogManager: HttpLogManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CLEAR_HTTP_LOGS) {
            runBlocking { httpLogManager.clearHttpLogs() }
            WiretapNotificationManager.clearHttpNotifications(context)
        }
    }
}
