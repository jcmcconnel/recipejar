package recipejar.macro

/**
 * Port of MacroTextAction parse + apply (without Swing dependencies).
 *
 * Expands a macro [template] against a [selection] string. Callers supply
 * [inputProvider] / [colorProvider] for interactive placeholders; returning null
 * cancels that stroke (empty string is written).
 */
object MacroProcessor {

    private enum class StrokeKind { TEXT, SELECTION, INPUT, COLOR }

    private data class Stroke(
        val kind: StrokeKind,
        val text: String = "",
        val prompt: String = "",
    )

    /**
     * Apply [template] replacing the editor selection with the expanded result.
     * Returns the expanded string (caller replaces selection / whole buffer with it).
     */
    fun applyMacro(
        template: String,
        selection: String,
        inputProvider: (prompt: String) -> String? = { null },
        colorProvider: (prompt: String) -> String? = { null },
    ): String {
        val parts = parse(template)
        val out = StringBuilder()
        for (part in parts) {
            when (part.kind) {
                StrokeKind.TEXT -> out.append(part.text)
                StrokeKind.SELECTION -> out.append(selection)
                StrokeKind.INPUT -> out.append(inputProvider(part.prompt) ?: "")
                StrokeKind.COLOR -> out.append(colorProvider(part.prompt) ?: "#000000")
            }
        }
        return out.toString()
    }

    /**
     * True if [token] is a recognized bracket command (case-insensitive).
     */
    internal fun isCommand(token: String): Boolean {
        val u = token.uppercase()
        return u == "[SELECTION]" ||
            u == "[COLOR]" || u.startsWith("[COLOR:") ||
            u == "[INPUT]" || u.startsWith("[INPUT:")
    }

    private fun parse(macroString: String): List<Stroke> {
        val parts = mutableListOf<Stroke>()
        val textStroke = StringBuilder()
        var i = 0
        while (i < macroString.length) {
            val c = macroString[i]
            if (c == '[') {
                val s = StringBuilder()
                s.append(c)
                i++
                while (i < macroString.length && macroString[i] != ']') {
                    // Unpaired nested '[': flush prior fragment as text (matches original).
                    if (macroString[i] == '[') {
                        textStroke.append(s)
                        s.clear()
                    }
                    s.append(macroString[i])
                    i++
                }
                if (i < macroString.length) {
                    s.append(macroString[i]) // closing ]
                    i++
                }
                val token = s.toString()
                if (isCommand(token)) {
                    if (textStroke.isNotEmpty()) {
                        parts.add(Stroke(StrokeKind.TEXT, text = textStroke.toString()))
                        textStroke.clear()
                    }
                    parts.add(commandStroke(token))
                } else {
                    textStroke.append(token)
                }
            } else {
                textStroke.append(c)
                i++
            }
        }
        if (textStroke.isNotEmpty()) {
            parts.add(Stroke(StrokeKind.TEXT, text = textStroke.toString()))
        }
        return parts
    }

    private fun commandStroke(token: String): Stroke {
        val u = token.uppercase()
        return when {
            u == "[SELECTION]" -> Stroke(StrokeKind.SELECTION)
            u == "[COLOR]" || u.startsWith("[COLOR:") -> {
                val prompt = if (u == "[COLOR]") {
                    "Select Color"
                } else {
                    // "[COLOR:" is 7 chars; drop trailing ']'
                    token.substring(7, token.length - 1)
                }
                Stroke(StrokeKind.COLOR, prompt = prompt)
            }
            u == "[INPUT]" || u.startsWith("[INPUT:") -> {
                val prompt = if (u == "[INPUT]") {
                    "Input:"
                } else {
                    token.substring(7, token.length - 1)
                }
                Stroke(StrokeKind.INPUT, prompt = prompt)
            }
            else -> Stroke(StrokeKind.TEXT, text = token)
        }
    }
}
