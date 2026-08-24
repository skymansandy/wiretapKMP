/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SocketContentTypeTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("isTextSearchable") {
        it("is true only for text frames") {
            // Both the match scanner and the bubble read this predicate; pinning
            // every enum value keeps the highlight and the match counter in step.
            SocketContentType.entries.associateWith { it.isTextSearchable() } shouldBe mapOf(
                SocketContentType.Text to true,
                SocketContentType.Binary to false,
                SocketContentType.Ping to false,
                SocketContentType.Pong to false,
                SocketContentType.Close to false,
            )
        }
    }
})
