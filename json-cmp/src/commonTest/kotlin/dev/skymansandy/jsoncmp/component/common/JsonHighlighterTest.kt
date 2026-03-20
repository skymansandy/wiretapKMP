package dev.skymansandy.jsoncmp.component.common

import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonHighlighterTest {

    private val colors = JsonCmpColors.Dark

    // ── Text content ──

    @Test
    fun highlightjsonPreservesOriginalText() {
        val text = """{"name": "John", "age": 30}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        result.text shouldBe text
    }

    @Test
    fun highlightjsonWithEmptyString() {
        val result = highlightJson("", searchQuery = "", colors = colors)

        result.text shouldBe ""
    }

    // ── Syntax highlighting spans ──

    @Test
    fun highlightjsonAppliesSpansToJsonText() {
        val text = """{"key": "value"}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        result.spanStyles.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesKeyColorToKeys() {
        val text = """{"name": "value"}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        // "name" is at positions 1-6 (after {), should have key color
        val keySpans = result.spanStyles.filter { it.item.color == colors.key }
        keySpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesStringColorToStringValues() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val stringSpans = result.spanStyles.filter { it.item.color == colors.string }
        stringSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesNumberColorToNumbers() {
        val text = """{"age": 42}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val numberSpans = result.spanStyles.filter { it.item.color == colors.number }
        numberSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesBooleanColorToBooleans() {
        val text = """{"flag": true}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val boolSpans = result.spanStyles.filter { it.item.color == colors.booleanColor }
        boolSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesNullColorToNull() {
        val text = """{"val": null}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val nullSpans = result.spanStyles.filter { it.item.color == colors.nullColor }
        nullSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonAppliesPunctuationColorToBraces() {
        val text = """{"a": 1}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val punctSpans = result.spanStyles.filter { it.item.color == colors.punctuation }
        punctSpans.shouldNotBeEmpty()
    }

    // ── Search highlighting ──

    @Test
    fun highlightjsonWithSearchQueryHighlightsMatches() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "John", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonSearchIsCaseInsensitive() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "john", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonHighlightsMultipleOccurrences() {
        val text = """{"a": "test", "b": "test"}"""
        val result = highlightJson(text, searchQuery = "test", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.size shouldBe 2
    }

    @Test
    fun highlightjsonWithBlankSearchQueryAddsNoHighlights() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.size shouldBe 0
    }

    @Test
    fun highlightjsonWithNoMatchingSearchAddsNoHighlights() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "xyz", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.size shouldBe 0
    }

    @Test
    fun highlightjsonSearchHighlightPositionIsCorrect() {
        val text = """{"name": "John"}"""
        val result = highlightJson(text, searchQuery = "John", colors = colors)

        val highlightSpans = result.spanStyles.filter { it.item.background == colors.highlight }
        highlightSpans.shouldNotBeEmpty()

        val span = highlightSpans.first()
        text.substring(span.start, span.end) shouldBe "John"
    }

    // ── Edge cases ──

    @Test
    fun highlightjsonHandlesNegativeNumbers() {
        val text = """{"val": -42}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val numberSpans = result.spanStyles.filter { it.item.color == colors.number }
        numberSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonHandlesScientificNotation() {
        val text = """{"val": 1.5e10}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val numberSpans = result.spanStyles.filter { it.item.color == colors.number }
        numberSpans.shouldNotBeEmpty()
    }

    @Test
    fun highlightjsonHandlesFalseBoolean() {
        val text = """{"flag": false}"""
        val result = highlightJson(text, searchQuery = "", colors = colors)

        val boolSpans = result.spanStyles.filter { it.item.color == colors.booleanColor }
        boolSpans.shouldNotBeEmpty()
    }
}
