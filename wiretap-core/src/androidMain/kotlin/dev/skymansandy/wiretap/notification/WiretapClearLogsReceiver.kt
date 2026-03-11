package dev.skymansandy.wiretap.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class WiretapClearLogsReceiver : BroadcastReceiver(), KoinComponent {

    private val orchestrator: WiretapOrchestrator by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WiretapNotificationManager.ACTION_CLEAR_LOGS) {
            orchestrator.clearLogs()
        }
    }
}
