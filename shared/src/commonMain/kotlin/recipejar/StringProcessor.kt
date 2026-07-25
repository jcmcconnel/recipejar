package recipejar

/**
 * Minimal port of original StringProcessor for use in HTML layer.
 * Preserves exact behaviors needed for filenames, labels, content.
 */
object StringProcessor {
    fun underscoreSpaces(cat: String): String {
        val s = StringBuilder()
        for (i in cat.indices) {
            val c = cat[i]
            if (c == ' ') s.append('_') else s.append(c)
        }
        return s.toString()
    }

    fun spaceUnderscores(cat: String): String {
        val s = StringBuilder()
        for (i in cat.indices) {
            val c = cat[i]
            if (c == '_') s.append(' ') else s.append(c)
        }
        return s.toString()
    }

    fun removeCarriageReturns(content: String): String {
        if (content.indexOf('\r') == -1) return content
        return content.replace("\r", "")
    }

    fun removeBadChars(s: String): String {
        var inProcess = removeChar(' ', s)
        inProcess = removeChar('\t', inProcess)
        inProcess = removeChar('\n', inProcess)
        inProcess = removeChar('\r', inProcess)
        inProcess = removeChar('\'', inProcess)
        inProcess = removeChar('\\', inProcess)
        inProcess = removeChar('/', inProcess)
        inProcess = removeChar('*', inProcess)
        inProcess = removeChar('?', inProcess)
        return inProcess.trim()
    }

    private fun removeChar(x: Char, from: String): String {
        val newString = StringBuilder()
        for (i in from.indices) {
            if (from[i] != x) newString.append(from[i])
        }
        return newString.toString()
    }
}
