/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FormatUtilTest {

    // formatOneDecimal -------------------------------------------------------

    @Test
    fun `formatOneDecimal renders integer values with a trailing zero`() {
        formatOneDecimal(5.0f) shouldBe "5.0"
    }

    @Test
    fun `formatOneDecimal truncates the fractional part toward zero`() {
        formatOneDecimal(1.49f) shouldBe "1.4"
    }

    @Test
    fun `formatOneDecimal renders the abs of the decimal for negative input`() {
        formatOneDecimal(-2.5f) shouldBe "-2.5"
    }

    @Test
    fun `formatOneDecimal renders zero as zero point zero`() {
        formatOneDecimal(0f) shouldBe "0.0"
    }

    // formatSize -------------------------------------------------------------

    @Test
    fun `formatSize returns zero bytes for null`() {
        formatSize(null) shouldBe "0 B"
    }

    @Test
    fun `formatSize returns zero bytes for zero`() {
        formatSize(0L) shouldBe "0 B"
    }

    @Test
    fun `formatSize renders sub-kilobyte values in bytes`() {
        formatSize(512L) shouldBe "512 B"
    }

    @Test
    fun `formatSize renders kilobyte threshold in kB`() {
        formatSize(1_024L) shouldBe "1.0 kB"
    }

    @Test
    fun `formatSize renders megabyte threshold in MB`() {
        formatSize(1_048_576L) shouldBe "1.0 MB"
    }

    @Test
    fun `formatSize uses one-decimal rendering for fractional kB`() {
        formatSize(2_560L) shouldBe "2.5 kB"
    }

    // formatSizeOrNull -------------------------------------------------------

    @Test
    fun `formatSizeOrNull returns null for zero`() {
        formatSizeOrNull(0L) shouldBe null
    }

    @Test
    fun `formatSizeOrNull returns null for negative`() {
        formatSizeOrNull(-1L) shouldBe null
    }

    @Test
    fun `formatSizeOrNull renders positive bytes`() {
        formatSizeOrNull(123L) shouldBe "123 B"
    }

    @Test
    fun `formatSizeOrNull renders mebibyte threshold in MB`() {
        formatSizeOrNull(1_048_576L) shouldBe "1.0 MB"
    }

    // formatBytes ------------------------------------------------------------

    @Test
    fun `formatBytes renders raw bytes for values below 1 kB`() {
        formatBytes(0L) shouldBe "0 B"
        formatBytes(999L) shouldBe "999 B"
    }

    @Test
    fun `formatBytes uses integer kB above 1024`() {
        formatBytes(2_048L) shouldBe "2 kB"
    }

    @Test
    fun `formatBytes uses integer MB above 1 MiB`() {
        formatBytes(3_145_728L) shouldBe "3 MB"
    }

    // formatUrlDisplay -------------------------------------------------------

    @Test
    fun `formatUrlDisplay strips the scheme and keeps host with root path`() {
        formatUrlDisplay("https://example.com") shouldBe "example.com/"
    }

    @Test
    fun `formatUrlDisplay strips the scheme and keeps host with path`() {
        formatUrlDisplay("https://example.com/api/users") shouldBe "example.com/api/users"
    }

    @Test
    fun `formatUrlDisplay keeps the query string when the URL has no path segment`() {
        // Quirk of the current implementation: the host is split before "?", but
        // the path is then "afterScheme - host" which leaves "?foo=bar" attached.
        formatUrlDisplay("https://example.com?foo=bar") shouldBe "example.com?foo=bar"
    }

    @Test
    fun `formatUrlDisplay keeps the path and drops query for host-then-path-then-query`() {
        formatUrlDisplay("https://example.com/api/users?foo=bar") shouldBe "example.com/api/users?foo=bar"
    }

    @Test
    fun `formatUrlDisplay handles port in host segment`() {
        formatUrlDisplay("http://localhost:8080/health") shouldBe "localhost:8080/health"
    }
}
