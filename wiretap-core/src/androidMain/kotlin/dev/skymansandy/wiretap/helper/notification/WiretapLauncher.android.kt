package dev.skymansandy.wiretap.helper.notification

import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.presentation.WiretapConsoleActivity

actual fun startWiretap() {
    val context = WiretapContextProvider.context
    val intent = Intent(context, WiretapConsoleActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

actual fun enableLaunchTool() {
    ProcessLifecycleOwner.get().lifecycle.addObserver(ShakeGestureListener())
}
