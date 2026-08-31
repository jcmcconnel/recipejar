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
 * Parse/serialize classic units.txt: `;` comments, blank lines ignored,
 * data lines `plural[,singular[,convA(factor)|convB(factor)…]]`.
 *
 * User edits go through [serialize] + host persistence (e.g. desktop units file);
 * the ingredient unit picker reloads from the live catalog after save.
 */
object UnitsCatalog {
    const val FILE_HEADER: String =
        ";These are the units that RecipeJar recognizes.\n" +
            ";You can add your own simply by typing them in below.\n" +
            ";Please note, units are used exactly as they are typed, spaces and all.\n" +
            ";Lines beginning with \";\" are comments, and will be ignored by the program.\n"

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
     * Write units back to classic units.txt shape (stable for grandchildren / plain text).
     * Sorted by plural (case-insensitive) to match [parse] ordering.
     */
    fun serialize(units: List<UnitDef>): String {
        val sb = StringBuilder()
        sb.append(FILE_HEADER).append('\n')
        for (u in units.sortedBy { it.plural.lowercase() }) {
            if (u.plural.isBlank()) continue
            sb.append(u.plural)
            val hasSingular = u.singular.isNotBlank()
            val hasConv = u.conversions.isNotEmpty()
            if (hasSingular || hasConv) {
                sb.append(',').append(u.singular)
            }
            if (hasConv) {
                sb.append(',')
                sb.append(
                    u.conversions.entries.joinToString("|") { (k, v) -> "$k($v)" },
                )
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    /** Insert or replace a unit by plural (case-insensitive match). Returns new sorted list. */
    fun upsert(units: List<UnitDef>, unit: UnitDef): List<UnitDef> {
        val plural = unit.plural.trim()
        if (plural.isEmpty()) return units.sortedBy { it.plural.lowercase() }
        val cleaned = unit.copy(plural = plural, singular = unit.singular.trim())
        val without = units.filterNot { it.plural.equals(plural, ignoreCase = true) }
        return (without + cleaned).sortedBy { it.plural.lowercase() }
    }

    /** Remove unit matching [plural] (case-insensitive). */
    fun remove(units: List<UnitDef>, plural: String): List<UnitDef> {
        val p = plural.trim()
        if (p.isEmpty()) return units
        return units.filterNot { it.plural.equals(p, ignoreCase = true) }
            .sortedBy { it.plural.lowercase() }
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

    /** True when [name] is present in the catalog (plural or singular). */
    fun contains(units: List<UnitDef>, name: String): Boolean {
        val n = name.trim()
        if (n.isEmpty()) return false
        return units.any { it.matches(n) }
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
