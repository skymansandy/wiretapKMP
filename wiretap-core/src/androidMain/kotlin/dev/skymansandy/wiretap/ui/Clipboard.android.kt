package dev.skymansandy.wiretap.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dev.skymansandy.wiretap.WiretapContextProvider

internal actual fun copyToClipboard(text: String) {
    val context = WiretapContextProvider.context
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Wiretap body", text))
}
