package dev.skymansandy.jsoncmp.helper.constants.colors

import dev.skymansandy.jsoncmp.model.JsonPart
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class JsonViewerColorsTest {

    private val colors = JsonCmpColors.Dark

    // ── partColor mapping ──

    @Test
    fun partcolorMapsKeyToKeyColor() {
        partColor(JsonPart.Key("\"name\""), colors) shouldBe colors.key
    }

    @Test
    fun partcolorMapsStrValToStringColor() {
        partColor(JsonPart.StrVal("\"hello\""), colors) shouldBe colors.string
    }

    @Test
    fun partcolorMapsNumValToNumberColor() {
        partColor(JsonPart.NumVal("42"), colors) shouldBe colors.number
    }

    @Test
    fun partcolorMapsBoolValToBooleanColor() {
        partColor(JsonPart.BoolVal("true"), colors) shouldBe colors.booleanColor
    }

    @Test
    fun partcolorMapsNullValToNullColor() {
        partColor(JsonPart.NullVal("null"), colors) shouldBe colors.nullColor
    }

    @Test
    fun partcolorMapsPunctToPunctuationColor() {
        partColor(JsonPart.Punct("{"), colors) shouldBe colors.punctuation
    }

    @Test
    fun partcolorMapsIndentToPunctuationColor() {
        partColor(JsonPart.Indent("  "), colors) shouldBe colors.punctuation
    }

    // ── Color schemes ──

    @Test
    fun allColorSchemesHaveDistinctBackgrounds() {
        val schemes = listOf(
            JsonCmpColors.Dark,
            JsonCmpColors.Light,
            JsonCmpColors.Monokai,
            JsonCmpColors.Dracula,
            JsonCmpColors.SolarizedDark,
        )
        val backgrounds = schemes.map { it.background }

        backgrounds.distinct().size shouldBe schemes.size
    }

    @Test
    fun eachColorSchemeHasAllRequiredFieldsSet() {
        val schemes = listOf(
            JsonCmpColors.Dark,
            JsonCmpColors.Light,
            JsonCmpColors.Monokai,
            JsonCmpColors.Dracula,
            JsonCmpColors.SolarizedDark,
        )

        schemes.forEach { scheme ->
            scheme.key shouldNotBe scheme.background
            scheme.string shouldNotBe scheme.background
            scheme.errorBackground shouldNotBe scheme.background
        }
    }

    @Test
    fun partcolorWorksWithAllColorSchemes() {
        val schemes = listOf(
            JsonCmpColors.Dark,
            JsonCmpColors.Light,
            JsonCmpColors.Monokai,
            JsonCmpColors.Dracula,
            JsonCmpColors.SolarizedDark,
        )
        val part = JsonPart.Key("\"test\"")

        schemes.forEach { scheme ->
            partColor(part, scheme) shouldBe scheme.key
        }
    }
}
