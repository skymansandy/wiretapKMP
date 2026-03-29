/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.skymansandy.wiretap.helper.notification.WiretapNotificationManager
import dev.skymansandy.wiretap.navigation.api.WiretapScreen
import dev.skymansandy.wiretap.ui.screens.WiretapConsole
import dev.skymansandy.wiretap.ui.theme.WiretapTheme

class WiretapConsoleActivity : ComponentActivity() {

    private var deepLinkScreen by mutableStateOf<WiretapScreen?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkScreen = parseDeepLinkScreen(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            WiretapTheme {
                WiretapConsole(
                    deepLinkScreen = deepLinkScreen,
                    onDeepLinkConsumed = { deepLinkScreen = null },
                    onBack = { finish() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseDeepLinkScreen(intent)?.let { deepLinkScreen = it }
    }

    private fun parseDeepLinkScreen(intent: Intent?): WiretapScreen? {
        val socketId = intent?.getLongExtra(WiretapNotificationManager.EXTRA_SOCKET_ID, -1L) ?: -1L
        if (socketId > 0) return WiretapScreen.SocketDetailScreen(socketId)
        return null
    }
}
