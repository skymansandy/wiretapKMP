/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LogShareUtilTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("shareFileName") {
        it("names the file per entry so back-to-back shares do not collide") {
            shareFileName(SOCKET_LOG_FILE_PREFIX, 12) shouldBe "wiretap_socket_log_12.txt"
            shareFileName(SOCKET_LOG_FILE_PREFIX, 13) shouldNotBe
                shareFileName(SOCKET_LOG_FILE_PREFIX, 12)
        }

        it("keeps each log type in its own namespace") {
            shareFileName(SSE_LOG_FILE_PREFIX, 1) shouldBe "wiretap_sse_log_1.txt"
            shareFileName(HTTP_LOG_FILE_PREFIX, 1) shouldBe "wiretap_http_log_1.txt"
        }
    }

    describe("exceedsShareTextLimit") {
        it("accepts text up to and including the limit") {
            exceedsShareTextLimit("") shouldBe false
            exceedsShareTextLimit("x".repeat(MAX_SHARE_TEXT_LENGTH)) shouldBe false
        }

        it("rejects text past the limit") {
            exceedsShareTextLimit("x".repeat(MAX_SHARE_TEXT_LENGTH + 1)) shouldBe true
        }

        it("keeps the limit inside the binder transaction budget") {
            // Two bytes per char as UTF-16, against a ~1 MB per-transaction cap.
            (MAX_SHARE_TEXT_LENGTH * 2) shouldBe 400_000
        }
    }
})
