package dev.skymansandy.wiretap.ui

import android.content.Intent
import dev.skymansandy.wiretap.WiretapContextProvider

internal actual fun shareNetworkLog(subject: String, text: String) {
    val context = WiretapContextProvider.context
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(
        Intent.createChooser(intent, "Share network log").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}
