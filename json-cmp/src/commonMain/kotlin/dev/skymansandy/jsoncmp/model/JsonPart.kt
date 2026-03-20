package dev.skymansandy.jsoncmp.model

internal sealed interface JsonPart {

    val text: String

    data class Key(override val text: String) : JsonPart

    data class StrVal(override val text: String) : JsonPart

    data class NumVal(override val text: String) : JsonPart

    data class BoolVal(override val text: String) : JsonPart

    data class NullVal(override val text: String) : JsonPart

    data class Punct(override val text: String) : JsonPart

    data class Indent(override val text: String) : JsonPart
}
