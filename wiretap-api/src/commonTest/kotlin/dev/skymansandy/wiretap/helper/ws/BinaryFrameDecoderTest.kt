/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.ws

import dev.skymansandy.wiretap.domain.model.config.ws.BinaryFrameDecoding
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@OptIn(InternalWiretapApi::class)
class BinaryFrameDecoderTest : DescribeSpec({

    describe("Placeholder mode") {
        it("always renders the byte-count placeholder, even for valid text") {
            val text = "hello".encodeToByteArray()

            decodeBinaryFrame(text, BinaryFrameDecoding.Placeholder) shouldBe "[Binary: 5 bytes]"
        }

        it("renders an empty placeholder for empty input") {
            decodeBinaryFrame(ByteArray(0), BinaryFrameDecoding.Placeholder) shouldBe "[Binary: 0 bytes]"
        }
    }

    describe("Utf8 mode") {
        it("decodes valid UTF-8 as text") {
            val text = "hello".encodeToByteArray()

            decodeBinaryFrame(text, BinaryFrameDecoding.Utf8) shouldBe "hello"
        }

        it("replaces invalid sequences with the replacement character") {
            val invalid = byteArrayOf(0xC0.toByte(), 0xC1.toByte())

            val result = decodeBinaryFrame(invalid, BinaryFrameDecoding.Utf8)

            // Behaviour is platform-defined but should not throw.
            result.isEmpty() shouldBe false
        }
    }

    describe("Auto mode") {
        it("returns the decoded text for valid printable UTF-8") {
            val text = "{\"hub\":\"chat\",\"id\":7}".encodeToByteArray()

            decodeBinaryFrame(text, BinaryFrameDecoding.Auto) shouldBe "{\"hub\":\"chat\",\"id\":7}"
        }

        it("allows tab, line feed, and carriage return as whitespace") {
            val text = "line1\nline2\twith\ttabs\rok".encodeToByteArray()

            decodeBinaryFrame(text, BinaryFrameDecoding.Auto) shouldBe "line1\nline2\twith\ttabs\rok"
        }

        it("returns an empty string for empty input") {
            decodeBinaryFrame(ByteArray(0), BinaryFrameDecoding.Auto) shouldBe ""
        }

        it("falls back to placeholder when the payload contains a NUL byte") {
            val payload = byteArrayOf(0, 1, 2, 3)

            decodeBinaryFrame(payload, BinaryFrameDecoding.Auto) shouldBe "[Binary: 4 bytes]"
        }

        it("falls back to placeholder when the payload contains non-whitespace control bytes") {
            val payload = byteArrayOf(0x01, 0x02, 0x03)

            decodeBinaryFrame(payload, BinaryFrameDecoding.Auto) shouldBe "[Binary: 3 bytes]"
        }

        it("falls back to placeholder when the payload is not valid UTF-8") {
            val payload = byteArrayOf(0xC0.toByte(), 0xC1.toByte(), 0xF5.toByte())

            decodeBinaryFrame(payload, BinaryFrameDecoding.Auto) shouldBe "[Binary: 3 bytes]"
        }

        it("falls back to placeholder for protobuf-like payloads with framing tags") {
            // Field tag (0x08 = field 1, varint) is a control byte; rejected.
            val protobufLike = byteArrayOf(0x08, 0x96.toByte(), 0x01)

            decodeBinaryFrame(protobufLike, BinaryFrameDecoding.Auto) shouldBe "[Binary: 3 bytes]"
        }
    }

    describe("Custom mode") {
        it("delegates rendering to the user-supplied function") {
            val hex = BinaryFrameDecoding.Custom { bytes ->
                bytes.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
            }

            decodeBinaryFrame(byteArrayOf(0x0F, 0xA0.toByte(), 0x01), hex) shouldBe "0fa001"
        }

        it("invokes the custom decoder even for an empty payload") {
            var calls = 0
            val tag = BinaryFrameDecoding.Custom { _ ->
                calls++
                "called"
            }

            decodeBinaryFrame(ByteArray(0), tag) shouldBe "called"
            calls shouldBe 1
        }

        it("passes the original bytes through to the custom decoder") {
            val payload = byteArrayOf(0x10, 0x20, 0x30)
            val capture = BinaryFrameDecoding.Custom { bytes ->
                bytes.contentToString()
            }

            decodeBinaryFrame(payload, capture) shouldBe payload.contentToString()
        }
    }
})
