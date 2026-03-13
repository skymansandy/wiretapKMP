package dev.skymansandy.wiretap.helper.notification

import android.content.Intent
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.presentation.WiretapActivity

actual fun startWiretap() {
    val context = WiretapContextProvider.context
    val intent = Intent(context, WiretapActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
