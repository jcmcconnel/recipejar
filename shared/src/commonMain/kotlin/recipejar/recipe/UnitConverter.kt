package recipejar.recipe

/**
 * Catalog-driven unit conversion (port of classic [recipejar.recipe.Unit] convert).
 * Factors live on the *from* unit as `targetPlural(factor)` entries.
 *
 * [factor] may be a scalar (`8`) or a linear `m+b` / `m-b` formula.
 * Mixed numbers and ranges (`1-2`) are accepted in [qty].
 */
object UnitConverter {

    fun findUnit(catalog: List<UnitDef>, name: String): UnitDef? {
        val n = name.trim()
        if (n.isEmpty()) return null
        return catalog.firstOrNull { it.matches(n) }
    }

    /**
     * Factor string to convert [from] into [to], or null when the catalog has no mapping.
     */
    fun conversionFactor(from: UnitDef, to: UnitDef): String? {
        val keys = buildList {
            add(to.plural)
            if (to.singular.isNotBlank()) add(to.singular)
        }
        for (key in keys) {
            from.conversions[key]?.let { return it }
            from.conversions.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.let { return it.value }
        }
        return null
    }

    /** Units that have at least one conversion factor (from-dropdown). */
    fun convertableUnits(catalog: List<UnitDef>): List<UnitDef> =
        catalog.filter { it.conversions.isNotEmpty() }

    /**
     * Target units listed on [from], resolved against [catalog] when possible
     * (unknown keys still appear so a user-typed conversion remains usable).
     */
    fun conversionTargets(from: UnitDef, catalog: List<UnitDef>): List<UnitDef> {
        val seen = linkedSetOf<String>()
        val out = mutableListOf<UnitDef>()
        for (key in from.conversions.keys) {
            val resolved = findUnit(catalog, key) ?: UnitDef(plural = key)
            val id = resolved.plural.lowercase()
            if (seen.add(id)) out.add(resolved)
        }
        return out
    }

    /**
     * Convert [qty] from [from] to [to] using catalog factors.
     * Returns null when no factor exists.
     */
    fun convert(qty: String, from: UnitDef, to: UnitDef): String? {
        val factor = conversionFactor(from, to) ?: return null
        return convert(qty, factor)
    }

    /**
     * Apply [factor] to [qty]. Prefer fractions when [qty] has no decimal point
     * (classic converter behavior); otherwise decimal `0.###`.
     */
    fun convert(qty: String, factor: String): String {
        val wantFraction = !qty.contains('.')
        return applyFactor(qty.trim(), factor.trim(), wantFraction)
    }

    internal fun parseMixedNumber(qty: String): Float? {
        val s = qty.trim()
        if (s.isEmpty()) return 0f
        return try {
            when {
                s.contains(' ') && s.contains('/') -> {
                    val space = s.indexOf(' ')
                    val slash = s.indexOf('/')
                    val whole = s.substring(0, space).toFloat()
                    val num = s.substring(space + 1, slash).toFloat()
                    val den = s.substring(slash + 1).toFloat()
                    if (den == 0f) null else whole + num / den
                }
                !s.contains(' ') && s.contains('/') -> {
                    val slash = s.indexOf('/')
                    val num = s.substring(0, slash).toFloat()
                    val den = s.substring(slash + 1).toFloat()
                    if (den == 0f) null else num / den
                }
                else -> s.toFloat()
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    internal fun decimalToFraction(value: Float): String {
        val whole = value.toInt()
        val frac = value - whole
        val known = when {
            almost(frac, 0.75f) -> "3/4"
            almost(frac, 0.5f) -> "1/2"
            frac > 0.3f && frac < 0.334f -> "1/3"
            almost(frac, 0.25f) -> "1/4"
            almost(frac, 0.2f) -> "1/5"
            almost(frac, 0.125f) -> "1/8"
            almost(frac, 0.0625f) -> "1/16"
            else -> null
        }
        return when {
            known != null && kotlin.math.abs(whole) > 0 -> "$whole $known"
            known != null -> known
            kotlin.math.abs(frac) < 1e-4f -> whole.toString()
            else -> formatDecimal(value)
        }
    }

    private fun applyFactor(qty: String, factor: String, outputFraction: Boolean): String {
        if (qty.contains('-') && qty.trim().split('-').size > 1) {
            val dash = qty.indexOf('-')
            val left = qty.substring(0, dash).trim()
            val right = qty.substring(dash + 1).trim()
            return applyFactor(left, factor, outputFraction) + "-" +
                applyFactor(right, factor, outputFraction)
        }
        val x = parseMixedNumber(qty) ?: return qty
        val result = evaluateFactor(x, factor) ?: return qty
        return if (outputFraction) decimalToFraction(result) else formatDecimal(result)
    }

    private fun evaluateFactor(x: Float, factor: String): Float? {
        return try {
            when {
                factor.contains('+') -> {
                    val parts = factor.split('+', limit = 2)
                    val m = parseMixedNumber(parts[0]) ?: return null
                    val b = parseMixedNumber(parts[1]) ?: return null
                    m * x + b
                }
                factor.contains('-') && !factor.startsWith('-') -> {
                    val parts = factor.split('-', limit = 2)
                    val m = parseMixedNumber(parts[0]) ?: return null
                    val b = parseMixedNumber(parts[1]) ?: return null
                    m * x - b
                }
                else -> {
                    val m = parseMixedNumber(factor) ?: return null
                    m * x
                }
            }
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun formatDecimal(value: Float): String {
        val rounded = (value * 1000f).toInt() / 1000f
        return if (kotlin.math.abs(rounded - rounded.toInt()) < 1e-4f) {
            rounded.toInt().toString()
        } else {
            // Trim trailing zeros without java.text (common code).
            val raw = ((rounded * 1000f).toInt()).toString()
            val sign = if (rounded < 0) "-" else ""
            val absInt = kotlin.math.abs((rounded * 1000f).toInt())
            val whole = absInt / 1000
            val frac = (absInt % 1000).toString().padStart(3, '0').trimEnd('0')
            if (frac.isEmpty()) "$sign$whole" else "$sign$whole.$frac"
        }
    }

    private fun almost(a: Float, b: Float): Boolean = kotlin.math.abs(a - b) < 1e-4f
}
