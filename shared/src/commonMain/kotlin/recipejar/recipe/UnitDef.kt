package recipejar.recipe

/**
 * A cooking/measurement unit from units.txt (port of classic [recipejar.recipe.Unit] data).
 * [plural] is the primary display form used in ingredient HTML.
 */
data class UnitDef(
    val plural: String,
    val singular: String = "",
    val conversions: Map<String, String> = emptyMap(),
) {
    /** Label for dropdowns and ingredient unit span (classic toString without singular mode). */
    fun displayName(): String = plural

    fun matches(name: String): Boolean {
        val n = name.trim()
        if (n.isEmpty()) return plural.isEmpty()
        return plural.equals(n, ignoreCase = true) || singular.equals(n, ignoreCase = true)
    }
}

/**
 * Parse classic units.txt: `;` comments, blank lines ignored,
 * data lines `plural[,singular[,convA(factor)|convB(factor)…]]`.
 */
object UnitsCatalog {
    fun parse(text: String): List<UnitDef> {
        val out = mutableListOf<UnitDef>()
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith(";")) continue
            val parts = splitCsvLimited(line, 3)
            val plural = parts.getOrNull(0)?.trim().orEmpty()
            if (plural.isEmpty()) continue
            val singular = parts.getOrNull(1)?.trim().orEmpty()
            val conversions = parts.getOrNull(2)?.let { parseConversions(it) } ?: emptyMap()
            out.add(UnitDef(plural = plural, singular = singular, conversions = conversions))
        }
        return out.sortedBy { it.plural.lowercase() }
    }

    /**
     * Display labels for a unit picker: blank first, then each unit plural.
     * If [current] is non-blank and not already listed, append it (preserve freeform values).
     */
    fun dropdownLabels(units: List<UnitDef>, current: String = ""): List<String> {
        val labels = mutableListOf("")
        units.forEach { labels.add(it.displayName()) }
        val c = current.trim()
        if (c.isNotEmpty() && labels.none { it.equals(c, ignoreCase = true) }) {
            labels.add(c)
        }
        return labels
    }

    private fun splitCsvLimited(line: String, maxParts: Int): List<String> {
        val parts = mutableListOf<String>()
        var start = 0
        var found = 0
        for (i in line.indices) {
            if (line[i] == ',' && found < maxParts - 1) {
                parts.add(line.substring(start, i))
                start = i + 1
                found++
            }
        }
        parts.add(line.substring(start))
        return parts
    }

    private fun parseConversions(s: String): Map<String, String> {
        if (s.isBlank()) return emptyMap()
        val map = linkedMapOf<String, String>()
        val segments = s.split('|')
        for (seg in segments) {
            val endKey = seg.lastIndexOf('(')
            val close = seg.lastIndexOf(')')
            if (endKey <= 0 || close <= endKey) continue
            val key = seg.substring(0, endKey).trim()
            val factor = seg.substring(endKey + 1, close).trim()
            if (key.isNotEmpty()) map[key] = factor
        }
        return map
    }
}
