package recipejar.html

/**
 * Maps recipe category/label names to alpha-index tab positions used by the shell.
 *
 * Index rules match [recipejar.persistence.FileSystemRecipeRepository] and the
 * Compose alpha tab: A–Z from the first character of the *category name*;
 * non-letter → "Other" (tab index 26).
 *
 * Program-footer category links use [RecipeSerializer.CATEGORY_LINK_SCHEME]
 * (`recipejar://category/<encoded-label>`).
 */
object CategoryNavigation {

    /** Encode a label for use in a program-footer href path segment. */
    fun encodeLabel(label: String): String {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return ""
        // Percent-encode reserved characters; keep unreserved (RFC 3986) as-is.
        val sb = StringBuilder(trimmed.length * 2)
        for (ch in trimmed) {
            when {
                ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' ||
                    ch == '-' || ch == '_' || ch == '.' || ch == '~' -> sb.append(ch)
                ch == ' ' -> sb.append("%20")
                else -> {
                    val bytes = ch.toString().encodeToByteArray()
                    for (b in bytes) {
                        sb.append('%')
                        val v = b.toInt() and 0xFF
                        sb.append("0123456789ABCDEF"[v ushr 4])
                        sb.append("0123456789ABCDEF"[v and 0x0F])
                    }
                }
            }
        }
        return sb.toString()
    }

    /** Decode a path segment produced by [encodeLabel]. */
    fun decodeLabel(encoded: String): String {
        if (encoded.isEmpty()) return ""
        val bytes = ArrayList<Byte>(encoded.length)
        var i = 0
        while (i < encoded.length) {
            val c = encoded[i]
            if (c == '%' && i + 2 < encoded.length) {
                val hi = encoded[i + 1].digitToIntOrNull(16)
                val lo = encoded[i + 2].digitToIntOrNull(16)
                if (hi != null && lo != null) {
                    bytes.add(((hi shl 4) or lo).toByte())
                    i += 3
                    continue
                }
            }
            if (c == '+') {
                bytes.add(' '.code.toByte())
            } else {
                bytes.add(c.code.toByte())
            }
            i++
        }
        return bytes.toByteArray().decodeToString()
    }

    /**
     * Extract category label from a program-footer href, or null if not a category link.
     * Accepts full URLs (`recipejar://category/Bread`) or bare paths.
     */
    fun labelFromHref(href: String?): String? {
        if (href.isNullOrBlank()) return null
        val prefix = RecipeSerializer.CATEGORY_LINK_SCHEME
        val idx = href.indexOf(prefix)
        if (idx < 0) return null
        val rest = href.substring(idx + prefix.length)
        val end = rest.indexOfFirst { it == '?' || it == '#' || it == '&' }
        val encoded = if (end >= 0) rest.substring(0, end) else rest
        val label = decodeLabel(encoded).trim()
        return label.takeIf { it.isNotEmpty() }
    }

    /**
     * First-letter bucket for a category name: 'A'..'Z' or '0' for Other.
     * Same rule as title letter buckets in the shell.
     */
    fun letterBucket(category: String): Char {
        val t = category.trim()
        if (t.isEmpty()) return '0'
        val c = t[0].uppercaseChar()
        return if (c in 'A'..'Z') c else '0'
    }

    /**
     * Alpha tab index for [category]: 0–25 for A–Z, 26 for Other.
     * Used when a program-footer category is activated in the readonly pane.
     */
    fun tabIndexForCategory(category: String): Int {
        val letter = letterBucket(category)
        return if (letter == '0') 26 else (letter - 'A')
    }

    /**
     * Given a program-footer activation (href or raw label), return the alphatab index
     * the shell should select. Null when the input is not a category activation.
     */
    fun tabIndexFromCategoryActivation(hrefOrLabel: String?): Int? {
        if (hrefOrLabel.isNullOrBlank()) return null
        val fromHref = labelFromHref(hrefOrLabel)
        val label = fromHref ?: hrefOrLabel.trim().takeIf {
            // Raw label path (Compose chip / unit tests): not a URL
            !it.contains("://") && !it.startsWith("index.html")
        } ?: return null
        return tabIndexForCategory(label)
    }
}
