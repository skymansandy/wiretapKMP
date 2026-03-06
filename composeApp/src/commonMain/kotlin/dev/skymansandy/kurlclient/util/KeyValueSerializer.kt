package dev.skymansandy.kurlclient.util

import dev.skymansandy.kurlclient.ui.KeyValueEntry

private const val ENTRY_SEP = "\u001E"   // record separator between entries
private const val FIELD_SEP = "\u001F"   // unit separator between fields

fun List<KeyValueEntry>.serialize(): String =
    joinToString(ENTRY_SEP) { "${it.key}${FIELD_SEP}${it.value}${FIELD_SEP}${it.enabled}" }

fun String.deserializeKeyValueEntries(startId: Long): Pair<List<KeyValueEntry>, Long> {
    if (isBlank()) return Pair(emptyList(), startId)
    var id = startId
    val entries = split(ENTRY_SEP).map { row ->
        val parts = row.split(FIELD_SEP)
        KeyValueEntry(
            id = id++,
            key = parts.getOrElse(0) { "" },
            value = parts.getOrElse(1) { "" },
            enabled = parts.getOrElse(2) { "true" } == "true"
        )
    }
    return Pair(entries, id)
}