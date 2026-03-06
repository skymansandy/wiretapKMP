package dev.skymansandy.kurlclient.ui.response

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Node model ────────────────────────────────────────────────────────────────

private sealed interface JNode {
    data class Obj(val id: Int, val entries: List<Pair<String, JNode>>) : JNode
    data class Arr(val id: Int, val items: List<JNode>) : JNode
    data class Str(val value: String) : JNode
    data class Num(val value: String) : JNode
    data class Bool(val value: Boolean) : JNode
    data object Null : JNode
}

// ── Parser ────────────────────────────────────────────────────────────────────

private class JsonParser(private val src: String) {
    private var pos = 0
    private var nextId = 0

    fun parse(): JNode { ws(); return value() }

    private fun value(): JNode = when {
        cur() == '{'                 -> parseObj()
        cur() == '['                 -> parseArr()
        cur() == '"'                 -> JNode.Str(strVal())
        src.startsWith("true",  pos) -> { pos += 4; JNode.Bool(true) }
        src.startsWith("false", pos) -> { pos += 5; JNode.Bool(false) }
        src.startsWith("null",  pos) -> { pos += 4; JNode.Null }
        else                         -> parseNum()
    }

    private fun parseObj(): JNode.Obj {
        val id = nextId++
        eat('{'); ws()
        val entries = mutableListOf<Pair<String, JNode>>()
        if (cur() != '}') {
            while (true) {
                ws()
                val k = strVal(); ws(); eat(':'); ws()
                entries += k to value()
                ws()
                if (cur() == ',') { pos++; ws() } else break
            }
        }
        eat('}')
        return JNode.Obj(id, entries)
    }

    private fun parseArr(): JNode.Arr {
        val id = nextId++
        eat('['); ws()
        val items = mutableListOf<JNode>()
        if (cur() != ']') {
            while (true) {
                ws(); items += value(); ws()
                if (cur() == ',') { pos++; ws() } else break
            }
        }
        eat(']')
        return JNode.Arr(id, items)
    }

    private fun strVal(): String {
        eat('"')
        val sb = StringBuilder()
        while (pos < src.length && src[pos] != '"') {
            if (src[pos] == '\\' && pos + 1 < src.length) {
                pos++
                when (src[pos]) {
                    '"'  -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/'  -> sb.append('/')
                    'n'  -> sb.append('\n')
                    'r'  -> sb.append('\r')
                    't'  -> sb.append('\t')
                    'b'  -> sb.append('\b')
                    'f'  -> sb.append('\u000C')
                    'u'  -> {
                        val h = src.substring(pos + 1, minOf(pos + 5, src.length))
                        sb.append(h.toInt(16).toChar())
                        pos += 4
                    }
                    else -> sb.append(src[pos])
                }
            } else {
                sb.append(src[pos])
            }
            pos++
        }
        eat('"')
        return sb.toString()
    }

    private fun parseNum(): JNode.Num {
        val s = pos
        if (cur() == '-') pos++
        while (pos < src.length && src[pos] in "0123456789.eE+-") pos++
        return JNode.Num(src.substring(s, pos))
    }

    private fun cur() = if (pos < src.length) src[pos] else '\u0000'
    private fun eat(c: Char) { if (cur() == c) pos++ }
    private fun ws() { while (pos < src.length && src[pos].isWhitespace()) pos++ }
}

// ── Colors (VS Code dark theme) ───────────────────────────────────────────────

private val cKey     = Color(0xFF9CDCFE)
private val cString  = Color(0xFFCE9178)
private val cNumber  = Color(0xFFB5CEA8)
private val cKeyword = Color(0xFF569CD6)
private val cPunct   = Color(0xFFAAAAAA)
private val cBracket = Color(0xFFFFD700)
private val cArrow   = Color(0xFF777777)

// ── Render lines ──────────────────────────────────────────────────────────────

private data class Line(
    val indent: Int,
    val text: AnnotatedString,
    val foldId: Int = -1,
    val folded: Boolean = false
)

private fun sp(color: Color) =
    SpanStyle(color = color, fontFamily = FontFamily.Monospace, fontSize = 13.sp)

private fun buildLines(root: JNode, folded: Set<Int>): List<Line> {
    val out = mutableListOf<Line>()
    emit(root, 0, out, folded, prefix = null, suffix = null)
    return out
}

private fun emit(
    node: JNode,
    indent: Int,
    out: MutableList<Line>,
    folded: Set<Int>,
    prefix: AnnotatedString?,
    suffix: String?
) {
    val pre = prefix ?: AnnotatedString("")
    fun sfx() = if (suffix != null)
        buildAnnotatedString { pushStyle(sp(cPunct)); append(suffix); pop() }
    else AnnotatedString("")

    when (node) {
        is JNode.Obj -> {
            val isFolded = node.id in folded
            out += Line(
                indent = indent,
                text = buildAnnotatedString {
                    append(pre)
                    pushStyle(sp(cBracket)); append("{"); pop()
                    if (isFolded) { pushStyle(sp(cPunct)); append("...}"); pop(); append(sfx()) }
                },
                foldId = node.id,
                folded = isFolded
            )
            if (!isFolded) {
                node.entries.forEachIndexed { i, (k, v) ->
                    val keyPre = buildAnnotatedString {
                        pushStyle(sp(cKey)); append("\"$k\""); pop()
                        pushStyle(sp(cPunct)); append(": "); pop()
                    }
                    emit(v, indent + 1, out, folded, keyPre, if (i < node.entries.lastIndex) "," else null)
                }
                out += Line(indent, buildAnnotatedString {
                    pushStyle(sp(cBracket)); append("}"); pop(); append(sfx())
                })
            }
        }
        is JNode.Arr -> {
            val isFolded = node.id in folded
            out += Line(
                indent = indent,
                text = buildAnnotatedString {
                    append(pre)
                    pushStyle(sp(cBracket)); append("["); pop()
                    if (isFolded) { pushStyle(sp(cPunct)); append("...]"); pop(); append(sfx()) }
                },
                foldId = node.id,
                folded = isFolded
            )
            if (!isFolded) {
                node.items.forEachIndexed { i, v ->
                    emit(v, indent + 1, out, folded, null, if (i < node.items.lastIndex) "," else null)
                }
                out += Line(indent, buildAnnotatedString {
                    pushStyle(sp(cBracket)); append("]"); pop(); append(sfx())
                })
            }
        }
        is JNode.Str  -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cString)); append("\"${node.value.escaped()}\""); pop(); append(sfx())
        })
        is JNode.Num  -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cNumber)); append(node.value); pop(); append(sfx())
        })
        is JNode.Bool -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cKeyword)); append(node.value.toString()); pop(); append(sfx())
        })
        JNode.Null    -> out += Line(indent, buildAnnotatedString {
            append(pre); pushStyle(sp(cKeyword)); append("null"); pop(); append(sfx())
        })
    }
}

private fun String.escaped() = replace("\\", "\\\\").replace("\"", "\\\"")
    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

// ── Composable ────────────────────────────────────────────────────────────────

@Composable
fun JsonViewer(json: String, modifier: Modifier = Modifier) {
    val parsed = remember(json) { runCatching { JsonParser(json).parse() }.getOrNull() }

    if (parsed == null) {
        Text(
            text = json,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier
        )
        return
    }

    var foldedIds by remember(json) { mutableStateOf(emptySet<Int>()) }
    val lines = remember(json, foldedIds) { buildLines(parsed, foldedIds) }

    LazyColumn(modifier = modifier) {
        itemsIndexed(lines, key = { i, _ -> i }) { _, line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (line.foldId >= 0) Modifier.clickable {
                            foldedIds = if (line.foldId in foldedIds)
                                foldedIds - line.foldId
                            else
                                foldedIds + line.foldId
                        } else Modifier
                    )
                    .padding(start = (line.indent * 16 + 4).dp, top = 1.dp, bottom = 1.dp)
            ) {
                Text(
                    text = if (line.foldId >= 0) if (line.folded) "▶ " else "▼ " else "  ",
                    fontSize = 9.sp,
                    color = cArrow,
                    fontFamily = FontFamily.Monospace
                )
                Text(text = line.text)
            }
        }
    }
}