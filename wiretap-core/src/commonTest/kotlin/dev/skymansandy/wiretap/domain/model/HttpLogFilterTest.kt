/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HttpLogFilterTest {

    @Test
    fun `default filter is inactive`() {
        val filter = HttpLogFilter()

        filter.isActive shouldBe false
        filter.activeCount shouldBe 0
    }

    @Test
    fun `populated statusGroups marks filter active`() {
        val filter = HttpLogFilter(statusGroups = setOf(StatusGroup.Success))

        filter.isActive shouldBe true
        filter.activeCount shouldBe 1
    }

    @Test
    fun `populated methods marks filter active`() {
        val filter = HttpLogFilter(methods = setOf("GET"))

        filter.isActive shouldBe true
        filter.activeCount shouldBe 1
    }

    @Test
    fun `populated sources marks filter active`() {
        val filter = HttpLogFilter(sources = setOf(ResponseSource.Mock))

        filter.isActive shouldBe true
        filter.activeCount shouldBe 1
    }

    @Test
    fun `populated domains marks filter active`() {
        val filter = HttpLogFilter(domains = setOf("example.com"))

        filter.isActive shouldBe true
        filter.activeCount shouldBe 1
    }

    @Test
    fun `activeCount counts each populated dimension once`() {
        val filter = HttpLogFilter(
            statusGroups = setOf(StatusGroup.Success),
            methods = setOf("GET", "POST"),
            sources = setOf(ResponseSource.Mock),
            domains = setOf("a.com", "b.com"),
        )

        filter.activeCount shouldBe 4
    }

    @Test
    fun `StatusGroup ranges cover 2xx through 5xx`() {
        StatusGroup.Success.statusMin shouldBe 200
        StatusGroup.Success.statusMax shouldBe 299
        StatusGroup.Redirect.statusMin shouldBe 300
        StatusGroup.Redirect.statusMax shouldBe 399
        StatusGroup.ClientError.statusMin shouldBe 400
        StatusGroup.ClientError.statusMax shouldBe 499
        StatusGroup.ServerError.statusMin shouldBe 500
        StatusGroup.ServerError.statusMax shouldBe 599
    }

    @Test
    fun `StatusGroup All has null bounds`() {
        StatusGroup.All.statusMin shouldBe null
        StatusGroup.All.statusMax shouldBe null
    }

    @Test
    fun `StatusGroup InProgress encodes the sentinel response code`() {
        StatusGroup.InProgress.statusMin shouldBe -2
        StatusGroup.InProgress.statusMax shouldBe -2
    }

    @Test
    fun `StatusGroup Failed covers the cancel and error sentinels`() {
        StatusGroup.Failed.statusMin shouldBe -1
        StatusGroup.Failed.statusMax shouldBe 0
    }
}
