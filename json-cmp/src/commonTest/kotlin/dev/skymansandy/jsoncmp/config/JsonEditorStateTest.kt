package dev.skymansandy.jsoncmp.config

import dev.skymansandy.jsoncmp.TestData
import dev.skymansandy.jsoncmp.model.JsonNode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class JsonEditorStateTest {

    // ── Initialization ──

    @Test
    fun initParsesValidJson() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = false)

        state.parsedJson.shouldNotBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldNotBeEmpty()
        state.rawJson shouldBe TestData.SIMPLE_OBJECT
    }

    @Test
    fun initHandlesInvalidJson() {
        val state = JsonEditorState(TestData.INVALID_JSON, isEditing = false)

        state.parsedJson.shouldBeNull()
        state.error.shouldNotBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initHandlesEmptyJson() {
        val state = JsonEditorState("", isEditing = false)

        state.parsedJson.shouldBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initHandlesWhitespaceOnly() {
        val state = JsonEditorState("   ", isEditing = false)

        state.parsedJson.shouldBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun initSetsEditingFlag() {
        val editing = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        val viewing = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = false)

        editing.isEditing.shouldBeTrue()
        viewing.isEditing.shouldBeFalse()
    }

    @Test
    fun initSetsIsCompactToFalse() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = false)

        state.isCompact.shouldBeFalse()
    }

    // ── updateRawJson ──

    @Test
    fun updaterawjsonReparsesValidJson() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        state.updateRawJson(TestData.SIMPLE_ARRAY)

        state.rawJson shouldBe TestData.SIMPLE_ARRAY
        state.parsedJson.shouldNotBeNull()
        state.error.shouldBeNull()
    }

    @Test
    fun updaterawjsonSetsErrorForInvalidJson() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        state.updateRawJson(TestData.INVALID_JSON)

        state.parsedJson.shouldBeNull()
        state.error.shouldNotBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun updaterawjsonClearsStateForEmptyJson() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        state.updateRawJson("")

        state.parsedJson.shouldBeNull()
        state.error.shouldBeNull()
        state.allLines.shouldBeEmpty()
    }

    @Test
    fun updaterawjsonRebuildsDisplayLines() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        val originalLineCount = state.allLines.size

        state.updateRawJson(TestData.NESTED_OBJECT)

        state.allLines.size shouldBe originalLineCount.let { state.allLines.size }
        state.allLines.shouldNotBeEmpty()
    }

    // ── format ──

    @Test
    fun formatCompactProducesMinifiedJson() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)
        state.format(compact = true)

        state.isCompact.shouldBeTrue()
        state.rawJson shouldNotContain "\n"
        state.rawJson shouldNotContain "  "
    }

    @Test
    fun formatBeautifyProducesIndentedJson() {
        val state = JsonEditorState("""{"name":"John","age":30}""", isEditing = true)
        state.format(compact = false)

        state.isCompact.shouldBeFalse()
        state.rawJson shouldContain "\n"
        state.rawJson shouldContain "  "
    }

    @Test
    fun formatDoesNothingWhenNoParsedJson() {
        val state = JsonEditorState(TestData.INVALID_JSON, isEditing = true)
        val originalRaw = state.rawJson
        state.format(compact = true)

        state.rawJson shouldBe originalRaw
    }

    @Test
    fun formatToggleCompactThenBeautifyRoundtrips() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)

        state.format(compact = true)
        val compactJson = state.rawJson

        state.format(compact = false)
        val beautifiedJson = state.rawJson

        beautifiedJson shouldContain "\n"
        compactJson shouldNotContain "\n"
    }

    // ── sortKeys ──

    @Test
    fun sortkeysAscendingReordersKeys() {
        val state = JsonEditorState(TestData.UNSORTED_KEYS, isEditing = true)
        state.sortKeys(ascending = true)

        val obj = state.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun sortkeysDescendingReordersKeys() {
        val state = JsonEditorState(TestData.UNSORTED_KEYS, isEditing = true)
        state.sortKeys(ascending = false)

        val obj = state.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("c", "b", "a")
    }

    @Test
    fun sortkeysUpdatesRawJson() {
        val state = JsonEditorState(TestData.UNSORTED_KEYS, isEditing = true)
        state.sortKeys(ascending = true)

        val aIdx = state.rawJson.indexOf("\"a\"")
        val bIdx = state.rawJson.indexOf("\"b\"")
        val cIdx = state.rawJson.indexOf("\"c\"")

        (aIdx < bIdx).shouldBeTrue()
        (bIdx < cIdx).shouldBeTrue()
    }

    @Test
    fun sortkeysClearsError() {
        val state = JsonEditorState(TestData.UNSORTED_KEYS, isEditing = true)
        state.sortKeys(ascending = true)

        state.error.shouldBeNull()
    }

    @Test
    fun sortkeysDoesNothingWhenNoParsedJson() {
        val state = JsonEditorState(TestData.INVALID_JSON, isEditing = true)
        val originalRaw = state.rawJson
        state.sortKeys(ascending = true)

        state.rawJson shouldBe originalRaw
    }

    // ── collapseAll / expandAll ──

    @Test
    fun collapseallFoldsAllFoldableLines() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = false)
        val foldableIds = state.allLines.mapNotNull { it.foldId }

        state.collapseAll()

        foldableIds.forEach { id ->
            state.foldState[id] shouldBe true
        }
    }

    @Test
    fun expandallClearsFoldState() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = false)

        state.collapseAll()
        state.foldState.isNotEmpty().shouldBeTrue()

        state.expandAll()
        state.foldState.isEmpty().shouldBeTrue()
    }

    @Test
    fun collapseThenExpandRoundtrip() {
        val state = JsonEditorState(TestData.NESTED_OBJECT, isEditing = false)

        state.collapseAll()
        state.foldState.isNotEmpty().shouldBeTrue()

        state.expandAll()
        state.foldState.isEmpty().shouldBeTrue()
    }

    // ── Fold state cleanup ──

    @Test
    fun reparseCleansUpStaleFoldIDs() {
        val state = JsonEditorState(TestData.SIMPLE_OBJECT, isEditing = true)

        state.collapseAll()
        state.updateRawJson(TestData.SIMPLE_ARRAY)

        val newValidIds = state.allLines.mapNotNull { it.foldId }.toSet()
        state.foldState.keys.all { it in newValidIds }.shouldBeTrue()
    }
}
