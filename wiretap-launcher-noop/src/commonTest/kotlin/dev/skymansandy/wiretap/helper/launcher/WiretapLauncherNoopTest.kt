/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import io.kotest.core.spec.style.DescribeSpec

class WiretapLauncherNoopTest : DescribeSpec({
    describe("launchWiretapConsole") {
        it("is a no-op and does not throw") {
            launchWiretapConsole()
        }
    }

    describe("enableWiretapLauncher") {
        it("is a no-op and does not throw") {
            enableWiretapLauncher()
        }
    }
})
