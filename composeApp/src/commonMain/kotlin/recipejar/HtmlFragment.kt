package recipejar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Renders recipe notes/procedure HTML fragments (macro output + legacy corpus)
 * as a Compose [AnnotatedString].
 *
 * Supported subset (matches shipped macros + typical RecipeJar body markup):
 * - Line breaks: `<br>`, `<br/>`
 * - Blocks: `<p>`, `<li>` (shown as bullet lines)
 * - Headings: `<h1>`–`<h6>` (bold + larger type)
 * - Emphasis: `<strong>`/`<b>`, `<em>`/`<i>`, `<u>`
 * - `<span style="…">` for `color:` and `text-decoration: underline`
 * - Links: `<a href="…">`
 * - Common entities: `&nbsp;`, `&amp;`, `&lt;`, `&gt;`, `&deg;`, numeric refs
 *
 * Unknown tags are ignored (content kept). Full documents / scripts are not a goal —
 * use [stripSimpleHtml] for plain-text fallbacks (titles, welcome, share).
 */
internal fun htmlFragmentToAnnotatedString(html: String): AnnotatedString {
    if (html.isBlank()) return AnnotatedString("")
    val tokens = tokenizeHtmlFragment(html)
    return buildAnnotatedString {
        val styleStack = ArrayDeque<OpenStyle>()
        styleStack.addLast(OpenStyle())
        var pendingBlockBreak = false
        var lastChar: Char? = null

        fun current() = styleStack.last()

        fun appendRaw(s: String) {
            if (s.isEmpty()) return
            append(s)
            lastChar = s.last()
        }

        fun emitText(raw: String) {
            if (raw.isEmpty()) return
            val decoded = decodeHtmlEntities(raw)
            if (decoded.isEmpty()) return
            val text = if (pendingBlockBreak) {
                val trimmedStart = decoded.trimStart()
                if (trimmedStart.isEmpty()) return
                if (length > 0) {
                    when (lastChar) {
                        '\n' -> { /* single newline already — add one more for paragraph */ appendRaw("\n") }
                        else -> appendRaw("\n\n")
                    }
                }
                pendingBlockBreak = false
                trimmedStart
            } else {
                decoded
            }
            appendStyled(this, text, current()) { ch -> lastChar = ch }
        }

        fun push(transform: (OpenStyle) -> OpenStyle) {
            styleStack.addLast(transform(current()))
        }

        fun pop() {
            if (styleStack.size > 1) styleStack.removeLast()
        }

        for (tok in tokens) {
            when (tok) {
                is HtmlTok.Text -> emitText(tok.value)
                is HtmlTok.Open -> {
                    val name = tok.name
                    val attrs = tok.attrs
                    when (name) {
                        "br" -> {
                            appendRaw("\n")
                            pendingBlockBreak = false
                        }
                        "p" -> {
                            pendingBlockBreak = length > 0
                            push { it }
                        }
                        "li" -> {
                            if (length > 0 && lastChar != '\n') appendRaw("\n")
                            appendRaw("• ")
                            pendingBlockBreak = false
                            push { it }
                        }
                        "strong", "b" -> push { it.copy(bold = true) }
                        "em", "i" -> push { it.copy(italic = true) }
                        "u" -> push { it.copy(underline = true) }
                        "a" -> {
                            val href = attrs["href"]?.trim().orEmpty()
                            push { it.copy(href = href.ifBlank { it.href }) }
                        }
                        "span" -> {
                            val styleAttr = attrs["style"].orEmpty()
                            val color = parseCssColor(styleAttr)
                            val underline = styleAttr.contains("underline", ignoreCase = true)
                            push {
                                it.copy(
                                    color = color ?: it.color,
                                    underline = it.underline || underline,
                                )
                            }
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            pendingBlockBreak = length > 0
                            val level = name.last().digitToInt()
                            push { it.copy(heading = level, bold = true) }
                        }
                        "hr" -> {
                            if (length > 0 && lastChar != '\n') appendRaw("\n")
                            appendRaw("\n")
                            pendingBlockBreak = false
                        }
                        // document wrappers — ignore, keep children
                        "html", "head", "body", "div", "ul", "ol" -> push { it }
                        "title", "meta", "script", "style" -> push { it }
                        else -> push { it }
                    }
                    if (tok.selfClosing && name != "br") {
                        pop()
                    }
                }
                is HtmlTok.Close -> {
                    when (tok.name) {
                        "p" -> {
                            pop()
                            pendingBlockBreak = true
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            pop()
                            pendingBlockBreak = true
                        }
                        "li" -> {
                            pop()
                            if (lastChar != '\n') appendRaw("\n")
                        }
                        else -> pop()
                    }
                }
            }
        }
    }
}

private data class OpenStyle(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val color: Color? = null,
    val href: String? = null,
    val heading: Int = 0,
)

private fun appendStyled(
    builder: AnnotatedString.Builder,
    text: String,
    style: OpenStyle,
    onLastChar: (Char) -> Unit,
) {
    if (text.isEmpty()) return
    val headingSize = when (style.heading) {
        1 -> 22.sp
        2 -> 18.sp
        3, 4, 5, 6 -> 16.sp
        else -> TextUnit.Unspecified
    }
    val span = SpanStyle(
        fontWeight = if (style.bold || style.heading > 0) FontWeight.Bold else null,
        fontStyle = if (style.italic) FontStyle.Italic else null,
        fontSize = headingSize,
        textDecoration = when {
            style.underline -> TextDecoration.Underline
            style.href != null -> TextDecoration.Underline
            else -> null
        },
        color = style.color ?: if (style.href != null) Color(0xFF1565C0) else Color.Unspecified,
    )
    val href = style.href
    if (!href.isNullOrBlank()) {
        builder.withLink(
            LinkAnnotation.Url(
                url = href,
                styles = TextLinkStyles(style = span),
            ),
        ) {
            append(text)
        }
    } else if (style.bold || style.italic || style.underline || style.color != null || style.heading > 0) {
        builder.withStyle(span) { append(text) }
    } else {
        builder.append(text)
    }
    onLastChar(text.last())
}

private sealed class HtmlTok {
    data class Text(val value: String) : HtmlTok()
    data class Open(val name: String, val attrs: Map<String, String>, val selfClosing: Boolean) : HtmlTok()
    data class Close(val name: String) : HtmlTok()
}

/**
 * Minimal HTML fragment tokenizer. Not a full HTML parser — good enough for
 * macro-wrapped recipe body text.
 */
private fun tokenizeHtmlFragment(html: String): List<HtmlTok> {
    val out = mutableListOf<HtmlTok>()
    var i = 0
    val n = html.length
    val textBuf = StringBuilder()

    fun flushText() {
        if (textBuf.isNotEmpty()) {
            out.add(HtmlTok.Text(textBuf.toString()))
            textBuf.clear()
        }
    }

    while (i < n) {
        val c = html[i]
        if (c == '<') {
            val close = html.indexOf('>', i + 1)
            if (close < 0) {
                textBuf.append(html.substring(i))
                break
            }
            val inside = html.substring(i + 1, close).trim()
            i = close + 1
            if (inside.isEmpty()) continue
            flushText()
            when {
                inside.startsWith("!--") -> {
                    // skip comment (may already be closed if --> inside)
                }
                inside.startsWith("/") -> {
                    val name = inside.drop(1).trim().substringBefore(' ').lowercase()
                    if (name.isNotEmpty()) out.add(HtmlTok.Close(name))
                }
                else -> {
                    val selfClosing = inside.endsWith("/")
                    val body = if (selfClosing) inside.dropLast(1).trim() else inside
                    val name = body.substringBefore(' ').substringBefore('\t').lowercase()
                    val attrPart = body.drop(name.length).trim()
                    out.add(HtmlTok.Open(name, parseAttrs(attrPart), selfClosing || name == "br"))
                }
            }
        } else {
            textBuf.append(c)
            i++
        }
    }
    flushText()
    return out
}

private fun parseAttrs(raw: String): Map<String, String> {
    if (raw.isBlank()) return emptyMap()
    val map = mutableMapOf<String, String>()
    // name="value" | name='value'
    val re = Regex("""([A-Za-z_:][\w:.-]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|(\S+))""")
    re.findAll(raw).forEach { m ->
        val key = m.groupValues[1].lowercase()
        val value = m.groupValues[2].ifEmpty { m.groupValues[3].ifEmpty { m.groupValues[4] } }
        map[key] = value
    }
    return map
}

internal fun decodeHtmlEntities(text: String): String {
    if (!text.contains('&')) return text
    var s = text
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&deg;", "°")
        .replace("&mdash;", "—")
        .replace("&ndash;", "–")
        .replace("&hellip;", "…")
    // Numeric entities
    s = Regex("&#x([0-9a-fA-F]+);").replace(s) { m ->
        m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: m.value
    }
    s = Regex("&#([0-9]+);").replace(s) { m ->
        m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: m.value
    }
    return s
}

/**
 * Parse `color: #rgb | #rrggbb | rgb(r,g,b)` from a CSS style attribute.
 * Returns null when no usable color is present.
 */
internal fun parseCssColor(styleAttr: String): Color? {
    if (styleAttr.isBlank()) return null
    val colorPart = Regex("""(?i)color\s*:\s*([^;]+)""").find(styleAttr)
        ?.groupValues?.get(1)?.trim()
        ?: return null
    return parseCssColorValue(colorPart)
}

internal fun parseCssColorValue(value: String): Color? {
    val v = value.trim()
    if (v.startsWith("#")) {
        val hex = v.drop(1)
        return when (hex.length) {
            3 -> {
                val r = hex[0].digitToIntOrNull(16) ?: return null
                val g = hex[1].digitToIntOrNull(16) ?: return null
                val b = hex[2].digitToIntOrNull(16) ?: return null
                Color(r * 17, g * 17, b * 17)
            }
            6, 8 -> {
                val rgb = hex.take(6).toLongOrNull(16) ?: return null
                Color(
                    red = ((rgb shr 16) and 0xFF).toInt(),
                    green = ((rgb shr 8) and 0xFF).toInt(),
                    blue = (rgb and 0xFF).toInt(),
                )
            }
            else -> null
        }
    }
    val rgb = Regex("""(?i)rgb\s*\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*\)""").find(v)
    if (rgb != null) {
        val r = rgb.groupValues[1].toInt().coerceIn(0, 255)
        val g = rgb.groupValues[2].toInt().coerceIn(0, 255)
        val b = rgb.groupValues[3].toInt().coerceIn(0, 255)
        return Color(r, g, b)
    }
    return when (v.lowercase()) {
        "red" -> Color.Red
        "blue" -> Color.Blue
        "green" -> Color(0xFF2E7D32)
        "black" -> Color.Black
        "white" -> Color.White
        "gray", "grey" -> Color.Gray
        "orange" -> Color(0xFFEF6C00)
        "purple" -> Color(0xFF7B1FA2)
        else -> null
    }
}
