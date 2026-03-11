package dev.skymansandy.wiretap

import android.content.Intent

actual fun startWiretap() {
    val context = WiretapContextProvider.context
    val intent = Intent(context, WiretapActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
