package dev.skymansandy.wiretap.ui

import android.content.Intent
import dev.skymansandy.wiretap.WiretapContextProvider
import dev.skymansandy.wiretap.model.NetworkLogEntry

internal actual fun shareNetworkLog(entry: NetworkLogEntry) {
    val context = WiretapContextProvider.context
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "${entry.method} ${entry.responseCode} - ${entry.url}")
        putExtra(Intent.EXTRA_TEXT, buildShareText(entry))
    }
    context.startActivity(
        Intent.createChooser(intent, "Share network log").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}
