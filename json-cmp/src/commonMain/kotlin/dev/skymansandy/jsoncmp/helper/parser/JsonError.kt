package dev.skymansandy.jsoncmp.helper.parser

data class JsonError(
    val message: String,
    val position: Int? = null,
)
