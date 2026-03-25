package dev.skymansandy.wiretap.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.skymansandy.wiretap.helper.launcher.WiretapNotificationManager
import dev.skymansandy.wiretap.ui.WiretapScreen
import dev.skymansandy.wiretap.ui.theme.WiretapTheme

class WiretapConsoleActivity : ComponentActivity() {

    private var initialSocketId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSocketId = extractSocketId(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            WiretapTheme {
                WiretapScreen(
                    onBack = { finish() },
                    initialSocketId = initialSocketId,
                    onInitialSocketConsumed = { initialSocketId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        extractSocketId(intent)?.let { initialSocketId = it }
    }

    private fun extractSocketId(intent: Intent?): Long? {
        val id = intent?.getLongExtra(WiretapNotificationManager.EXTRA_SOCKET_ID, -1L) ?: -1L
        return if (id > 0) id else null
    }
}
