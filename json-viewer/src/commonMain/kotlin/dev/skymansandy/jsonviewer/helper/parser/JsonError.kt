package dev.skymansandy.jsonviewer.helper.parser

data class JsonError(
    val message: String,
    val position: Int? = null,
)
