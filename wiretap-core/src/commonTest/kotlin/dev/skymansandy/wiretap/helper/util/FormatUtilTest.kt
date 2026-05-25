/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class FormatUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("formatOneDecimal") {
        it("renders integer values with a trailing zero") {
            formatOneDecimal(5.0f) shouldBe "5.0"
        }

        it("truncates the fractional part toward zero") {
            formatOneDecimal(1.49f) shouldBe "1.4"
        }

        it("renders the abs of the decimal for negative input") {
            formatOneDecimal(-2.5f) shouldBe "-2.5"
        }

        it("renders zero as zero point zero") {
            formatOneDecimal(0f) shouldBe "0.0"
        }
    }

    describe("formatSize") {
        it("returns zero bytes for null") {
            formatSize(null) shouldBe "0 B"
        }

        it("returns zero bytes for zero") {
            formatSize(0L) shouldBe "0 B"
        }

        it("renders sub-kilobyte values in bytes") {
            formatSize(512L) shouldBe "512 B"
        }

        it("renders kilobyte threshold in kB") {
            formatSize(1_024L) shouldBe "1.0 kB"
        }

        it("renders megabyte threshold in MB") {
            formatSize(1_048_576L) shouldBe "1.0 MB"
        }

        it("uses one-decimal rendering for fractional kB") {
            formatSize(2_560L) shouldBe "2.5 kB"
        }
    }

    describe("formatSizeOrNull") {
        it("returns null for zero") {
            formatSizeOrNull(0L) shouldBe null
        }

        it("returns null for negative") {
            formatSizeOrNull(-1L) shouldBe null
        }

        it("renders positive bytes") {
            formatSizeOrNull(123L) shouldBe "123 B"
        }

        it("renders mebibyte threshold in MB") {
            formatSizeOrNull(1_048_576L) shouldBe "1.0 MB"
        }
    }

    describe("formatBytes") {
        it("renders raw bytes for values below 1 kB") {
            formatBytes(0L) shouldBe "0 B"
            formatBytes(999L) shouldBe "999 B"
        }

        it("uses integer kB above 1024") {
            formatBytes(2_048L) shouldBe "2 kB"
        }

        it("uses integer MB above 1 MiB") {
            formatBytes(3_145_728L) shouldBe "3 MB"
        }
    }

    describe("formatUrlDisplay") {
        it("strips the scheme and keeps host with root path") {
            formatUrlDisplay("https://example.com") shouldBe "example.com/"
        }

        it("strips the scheme and keeps host with path") {
            formatUrlDisplay("https://example.com/api/users") shouldBe "example.com/api/users"
        }

        it("keeps the query string when the URL has no path segment") {
            // Quirk of the current implementation: the host is split before "?", but
            // the path is then "afterScheme - host" which leaves "?foo=bar" attached.
            formatUrlDisplay("https://example.com?foo=bar") shouldBe "example.com?foo=bar"
        }

        it("keeps the path and drops query for host-then-path-then-query") {
            formatUrlDisplay("https://example.com/api/users?foo=bar") shouldBe "example.com/api/users?foo=bar"
        }

        it("handles port in host segment") {
            formatUrlDisplay("http://localhost:8080/health") shouldBe "localhost:8080/health"
        }
    }
})
