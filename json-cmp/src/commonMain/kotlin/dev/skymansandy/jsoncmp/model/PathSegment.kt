package dev.skymansandy.jsoncmp.model

sealed interface PathSegment {

    data class Key(val name: String) : PathSegment

    data class Index(val idx: Int) : PathSegment
}
