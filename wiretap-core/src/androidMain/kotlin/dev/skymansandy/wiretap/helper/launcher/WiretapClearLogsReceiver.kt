package dev.skymansandy.wiretap.helper.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.skymansandy.wiretap.di.WiretapKoinContext
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class WiretapClearLogsReceiver : BroadcastReceiver(), KoinComponent {

    override fun getKoin(): Koin = WiretapKoinContext.koin

    private val orchestrator: WiretapOrchestrator by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WiretapNotificationManager.ACTION_CLEAR_LOGS) {
            runBlocking { orchestrator.clearHttpLogs() }
        }
    }
}
