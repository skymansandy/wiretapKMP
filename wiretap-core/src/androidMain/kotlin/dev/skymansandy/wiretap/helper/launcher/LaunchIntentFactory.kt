/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import android.content.Intent
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import dev.skymansandy.wiretap.presentation.WiretapConsoleActivity

internal fun getLaunchIntent(): Intent {
    val context = WiretapContextProvider.context
    return Intent(context, WiretapConsoleActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
