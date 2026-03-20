package dev.skymansandy.wiretap.helper.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FormatUtilTest {

    // region formatOneDecimal

    @Test
    fun `formatOneDecimal - whole number`() {
        formatOneDecimal(5.0f) shouldBe "5.0"
    }

    @Test
    fun `formatOneDecimal - with decimal`() {
        formatOneDecimal(3.7f) shouldBe "3.7"
    }

    @Test
    fun `formatOneDecimal - truncates to one decimal`() {
        formatOneDecimal(2.99f) shouldBe "2.9"
    }

    @Test
    fun `formatOneDecimal - zero`() {
        formatOneDecimal(0.0f) shouldBe "0.0"
    }

    // endregion

    // region formatSize

    @Test
    fun `formatSize - null returns 0 B`() {
        formatSize(null) shouldBe "0 B"
    }

    @Test
    fun `formatSize - zero returns 0 B`() {
        formatSize(0L) shouldBe "0 B"
    }

    @Test
    fun `formatSize - bytes`() {
        formatSize(500L) shouldBe "500 B"
    }

    @Test
    fun `formatSize - kilobytes`() {
        formatSize(2048L) shouldBe "2.0 kB"
    }

    @Test
    fun `formatSize - megabytes`() {
        formatSize(2_097_152L) shouldBe "2.0 MB"
    }

    @Test
    fun `formatSize - just above kB threshold`() {
        formatSize(1024L) shouldBe "1.0 kB"
    }

    @Test
    fun `formatSize - just above MB threshold`() {
        formatSize(1_048_576L) shouldBe "1.0 MB"
    }

    // endregion

    // region formatBytes

    @Test
    fun `formatBytes - bytes`() {
        formatBytes(100L) shouldBe "100 B"
    }

    @Test
    fun `formatBytes - kilobytes`() {
        formatBytes(2048L) shouldBe "2 kB"
    }

    @Test
    fun `formatBytes - megabytes`() {
        formatBytes(2_097_152L) shouldBe "2 MB"
    }

    // endregion
}
