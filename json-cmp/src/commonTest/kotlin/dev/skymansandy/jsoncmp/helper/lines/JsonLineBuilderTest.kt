package dev.skymansandy.jsoncmp.helper.lines

import dev.skymansandy.jsoncmp.TestData
import dev.skymansandy.jsoncmp.model.FoldType
import dev.skymansandy.jsoncmp.model.JsonNode
import dev.skymansandy.jsoncmp.model.JsonPart
import dev.skymansandy.jsoncmp.model.PathSegment
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonLineBuilderTest {

    private fun lineText(line: dev.skymansandy.jsoncmp.model.JsonLine) =
        line.parts.joinToString("") { it.text }

    // ── Simple structures ──

    @Test
    fun buildLinesForEmptyObject() {
        val lines = buildDisplayLines(JsonNode.JObject(emptyList()))

        lines.shouldHaveSize(1)
        lineText(lines[0]) shouldBe "{}"
        lines[0].foldId.shouldBeNull()
        lines[0].depth shouldBe 0
    }

    @Test
    fun buildLinesForEmptyArray() {
        val lines = buildDisplayLines(JsonNode.JArray(emptyList()))

        lines.shouldHaveSize(1)
        lineText(lines[0]) shouldBe "[]"
        lines[0].foldId.shouldBeNull()
    }

    @Test
    fun buildLinesForSimpleObject() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        // { + "name": "John", + "age": 30 + }
        lines.shouldHaveSize(4)
        lineText(lines[0]) shouldBe "{"
        lineText(lines[1]) shouldBe """  "name": "John","""
        lineText(lines[2]) shouldBe """  "age": 30"""
        lineText(lines[3]) shouldBe "}"
    }

    @Test
    fun buildLinesForSimpleArray() {
        val lines = buildDisplayLines(TestData.simpleArrayNode)

        // [ + 1, + 2, + 3 + ]
        lines.shouldHaveSize(5)
        lineText(lines[0]) shouldBe "["
        lineText(lines[1]) shouldBe "  1,"
        lineText(lines[2]) shouldBe "  2,"
        lineText(lines[3]) shouldBe "  3"
        lineText(lines[4]) shouldBe "]"
    }

    // ── Line numbers ──

    @Test
    fun lineNumbersAreSequentialStartingAt1() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines.forEachIndexed { idx, line ->
            line.lineNumber shouldBe idx + 1
        }
    }

    // ── Depth tracking ──

    @Test
    fun depthTracksNestingLevel() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].depth shouldBe 0 // {
        lines[1].depth shouldBe 1 // "name": "John",
        lines[2].depth shouldBe 1 // "age": 30
        lines[3].depth shouldBe 0 // }
    }

    @Test
    fun nestedDepthTracksCorrectly() {
        val lines = buildDisplayLines(TestData.nestedObjectNode)

        lines[0].depth shouldBe 0 // {
        lines[1].depth shouldBe 1 // "user": {
        lines[2].depth shouldBe 2 // "name": "John",
        lines[3].depth shouldBe 2 // "address": {
        lines[4].depth shouldBe 3 // "city": "NYC"
    }

    // ── Fold IDs ──

    @Test
    fun nonEmptyObjectsGetFoldIDs() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].foldId.shouldNotBeNull()
        lines[0].foldType shouldBe FoldType.Object
    }

    @Test
    fun nonEmptyArraysGetFoldIDs() {
        val lines = buildDisplayLines(TestData.simpleArrayNode)

        lines[0].foldId.shouldNotBeNull()
        lines[0].foldType shouldBe FoldType.Array
    }

    @Test
    fun emptyObjectsDoNotGetFoldIDs() {
        val lines = buildDisplayLines(JsonNode.JObject(emptyList()))

        lines[0].foldId.shouldBeNull()
        lines[0].foldType.shouldBeNull()
    }

    @Test
    fun primitiveLinesDoNotGetFoldIDs() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[1].foldId.shouldBeNull() // "name": "John",
        lines[2].foldId.shouldBeNull() // "age": 30
    }

    @Test
    fun foldIDsAreUnique() {
        val node = JsonNode.JObject(
            listOf(
                "a" to JsonNode.JObject(listOf("x" to JsonNode.JNumber("1"))),
                "b" to JsonNode.JArray(listOf(JsonNode.JNumber("2"))),
            ),
        )
        val lines = buildDisplayLines(node)
        val foldIds = lines.mapNotNull { it.foldId }

        foldIds shouldBe foldIds.distinct()
    }

    // ── Parent fold IDs ──

    @Test
    fun rootLevelLinesHaveEmptyParentFoldIDs() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].parentFoldIds.shouldBeEmpty() // {
    }

    @Test
    fun childLinesTrackParentFoldIDs() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)
        val rootFoldId = lines[0].foldId!!

        lines[1].parentFoldIds shouldBe listOf(rootFoldId)
        lines[2].parentFoldIds shouldBe listOf(rootFoldId)
        lines[3].parentFoldIds shouldBe listOf(rootFoldId) // closing }
    }

    @Test
    fun nestedChildrenAccumulateParentFoldIDs() {
        val lines = buildDisplayLines(TestData.nestedObjectNode)
        val rootFoldId = lines[0].foldId!!
        val userFoldId = lines[1].foldId!!

        lines[2].parentFoldIds shouldBe listOf(rootFoldId, userFoldId)
    }

    // ── Fold child count ──

    @Test
    fun foldChildCountMatchesNumberOfDirectChildren() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].foldChildCount shouldBe 2 // "name" and "age"
    }

    @Test
    fun arrayFoldChildCountMatchesElementCount() {
        val lines = buildDisplayLines(TestData.simpleArrayNode)

        lines[0].foldChildCount shouldBe 3 // 1, 2, 3
    }

    // ── Folded content ──

    @Test
    fun foldedContentContainsAllChildLineText() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)
        val foldedContent = lines[0].foldedContent

        foldedContent.shouldNotBeNull()
        foldedContent.contains("\"name\"").shouldBeTrue()
        foldedContent.contains("\"age\"").shouldBeTrue()
    }

    // ── Closing brackets ──

    @Test
    fun closingBracketsAreMarked() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].isClosingBracket.shouldBeFalse() // {
        lines[1].isClosingBracket.shouldBeFalse() // "name": "John",
        lines[3].isClosingBracket.shouldBeTrue() // }
    }

    // ── Path tracking ──

    @Test
    fun rootPathIsEmpty() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[0].path.shouldBeEmpty()
    }

    @Test
    fun objectFieldPathsUseKeySegment() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lines[1].path shouldBe listOf(PathSegment.Key("name"))
        lines[2].path shouldBe listOf(PathSegment.Key("age"))
    }

    @Test
    fun arrayElementPathsUseIndexSegment() {
        val lines = buildDisplayLines(TestData.simpleArrayNode)

        lines[1].path shouldBe listOf(PathSegment.Index(0))
        lines[2].path shouldBe listOf(PathSegment.Index(1))
        lines[3].path shouldBe listOf(PathSegment.Index(2))
    }

    // ── JsonPart types ──

    @Test
    fun objectFieldLineHasCorrectPartTypes() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)
        val nameLine = lines[1] // "name": "John",

        nameLine.parts.any { it is JsonPart.Indent }.shouldBeTrue()
        nameLine.parts.any { it is JsonPart.Key }.shouldBeTrue()
        nameLine.parts.any { it is JsonPart.Punct }.shouldBeTrue()
        nameLine.parts.any { it is JsonPart.StrVal }.shouldBeTrue()
    }

    @Test
    fun numberValueLineHasNumValPart() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)
        val ageLine = lines[2] // "age": 30

        ageLine.parts.any { it is JsonPart.NumVal }.shouldBeTrue()
    }

    @Test
    fun booleanValueProducesBoolValPart() {
        val node = JsonNode.JObject(listOf("flag" to JsonNode.JBoolean(true)))
        val lines = buildDisplayLines(node)

        lines[1].parts.any { it is JsonPart.BoolVal }.shouldBeTrue()
    }

    @Test
    fun nullValueProducesNullValPart() {
        val node = JsonNode.JObject(listOf("val" to JsonNode.JNull))
        val lines = buildDisplayLines(node)

        lines[1].parts.any { it is JsonPart.NullVal }.shouldBeTrue()
    }

    // ── Commas ──

    @Test
    fun nonLastItemsHaveComma() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lineText(lines[1]).shouldBe("""  "name": "John",""")
    }

    @Test
    fun lastItemHasNoComma() {
        val lines = buildDisplayLines(TestData.simpleObjectNode)

        lineText(lines[2]).shouldBe("""  "age": 30""")
    }
}
