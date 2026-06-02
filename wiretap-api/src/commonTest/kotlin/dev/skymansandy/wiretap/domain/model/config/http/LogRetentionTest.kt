/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.http

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LogRetentionTest : DescribeSpec({

    describe("singleton variants") {
        it("Forever is a single instance") {
            (LogRetention.Forever === LogRetention.Forever) shouldBe true
        }

        it("AppSession is a single instance") {
            (LogRetention.AppSession === LogRetention.AppSession) shouldBe true
        }

        it("Forever and AppSession are not the same instance") {
            LogRetention.Forever shouldNotBe LogRetention.AppSession
        }
    }

    describe("Days") {
        it("exposes the configured day count") {
            LogRetention.Days(days = 7).days shouldBe 7
        }

        it("compares by value (data class equality)") {
            LogRetention.Days(7) shouldBe LogRetention.Days(7)
        }

        it("distinguishes different day counts") {
            LogRetention.Days(7) shouldNotBe LogRetention.Days(14)
        }
    }
})
