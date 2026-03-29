/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import android.content.Intent
import androidx.lifecycle.ProcessLifecycleOwner
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.presentation.WiretapConsoleActivity

actual fun launchWiretapConsole() {
    val context = WiretapContextProvider.context
    val intent = getLaunchIntent()
    context.startActivity(intent)
}

internal fun getLaunchIntent(): Intent {
    val context = WiretapContextProvider.context
    return Intent(context, WiretapConsoleActivity::class.java).apply {

        action = Intent.ACTION_VIEW
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

actual fun enableWiretapLauncher() {
    ProcessLifecycleOwner.get().lifecycle.addObserver(ShakeGestureListener())
}
