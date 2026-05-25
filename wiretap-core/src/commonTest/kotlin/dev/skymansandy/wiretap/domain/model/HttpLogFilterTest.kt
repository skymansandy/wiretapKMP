/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class HttpLogFilterTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("HttpLogFilter") {
        it("default filter is inactive") {
            val filter = HttpLogFilter()

            filter.isActive shouldBe false
            filter.activeCount shouldBe 0
        }

        it("populated statusGroups marks filter active") {
            val filter = HttpLogFilter(statusGroups = setOf(StatusGroup.Success))

            filter.isActive shouldBe true
            filter.activeCount shouldBe 1
        }

        it("populated methods marks filter active") {
            val filter = HttpLogFilter(methods = setOf("GET"))

            filter.isActive shouldBe true
            filter.activeCount shouldBe 1
        }

        it("populated sources marks filter active") {
            val filter = HttpLogFilter(sources = setOf(ResponseSource.Mock))

            filter.isActive shouldBe true
            filter.activeCount shouldBe 1
        }

        it("populated domains marks filter active") {
            val filter = HttpLogFilter(domains = setOf("example.com"))

            filter.isActive shouldBe true
            filter.activeCount shouldBe 1
        }

        it("activeCount counts each populated dimension once") {
            val filter = HttpLogFilter(
                statusGroups = setOf(StatusGroup.Success),
                methods = setOf("GET", "POST"),
                sources = setOf(ResponseSource.Mock),
                domains = setOf("a.com", "b.com"),
            )

            filter.activeCount shouldBe 4
        }
    }

    describe("StatusGroup") {
        it("ranges cover 2xx through 5xx") {
            StatusGroup.Success.statusMin shouldBe 200
            StatusGroup.Success.statusMax shouldBe 299
            StatusGroup.Redirect.statusMin shouldBe 300
            StatusGroup.Redirect.statusMax shouldBe 399
            StatusGroup.ClientError.statusMin shouldBe 400
            StatusGroup.ClientError.statusMax shouldBe 499
            StatusGroup.ServerError.statusMin shouldBe 500
            StatusGroup.ServerError.statusMax shouldBe 599
        }

        it("All has null bounds") {
            StatusGroup.All.statusMin shouldBe null
            StatusGroup.All.statusMax shouldBe null
        }

        it("InProgress encodes the sentinel response code") {
            StatusGroup.InProgress.statusMin shouldBe -2
            StatusGroup.InProgress.statusMax shouldBe -2
        }

        it("Failed covers the cancel and error sentinels") {
            StatusGroup.Failed.statusMin shouldBe -1
            StatusGroup.Failed.statusMax shouldBe 0
        }
    }
})
