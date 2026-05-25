/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import kotlin.test.Test

class WiretapLauncherNoopTest {

    @Test
    fun `launchWiretapConsole is a no-op and does not throw`() {
        launchWiretapConsole()
    }

    @Test
    fun `enableWiretapLauncher is a no-op and does not throw`() {
        enableWiretapLauncher()
    }
}
