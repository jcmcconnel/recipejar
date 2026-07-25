package recipejar.search

/**
 * Which recipe fields participate in a search.
 * Basic scope: titles + labels; notes/ingredients/procedure optional.
 */
enum class SearchScope {
    TITLES,
    LABELS,
    NOTES,
    INGREDIENTS,
    PROCEDURE,
}

/** Catalog entry used by [filterRecipesByQuery] (filename key + display title). */
data class SearchCatalogEntry(
    val filename: String,
    val title: String,
)

/** One hit from [filterRecipesByQuery]. */
data class SearchResult(
    val filename: String,
    val title: String,
    val matchHint: String,
)

/**
 * Sort key aligned with repository index lists: ASCII A–Z folded to lowercase.
 */
fun searchTitleSortKey(title: String): String = buildString(title.length) {
    for (c in title) {
        append(if (c in 'A'..'Z') c + ('a' - 'A') else c)
    }
}

/**
 * Substring (case-insensitive) filter over catalog + optional field text.
 *
 * [fieldText] maps filename → per-scope searchable text (labels, notes, …).
 * Title/filename matching uses [SearchCatalogEntry] when [SearchScope.TITLES] is active.
 */
fun filterRecipesByQuery(
    recipes: List<SearchCatalogEntry>,
    query: String,
    scopes: Set<SearchScope>,
    fieldText: Map<String, Map<SearchScope, String>> = emptyMap(),
): List<SearchResult> {
    val q = query.trim()
    if (q.isEmpty() || scopes.isEmpty()) return emptyList()
    val needle = q.lowercase()
    val results = mutableListOf<SearchResult>()
    for (item in recipes) {
        val hints = mutableListOf<String>()
        if (SearchScope.TITLES in scopes) {
            val t = item.title.ifBlank { item.filename }
            if (t.lowercase().contains(needle) || item.filename.lowercase().contains(needle)) {
                hints.add("title")
            }
        }
        val fields = fieldText[item.filename]
        if (fields != null) {
            for (scope in scopes) {
                if (scope == SearchScope.TITLES) continue
                val text = fields[scope] ?: continue
                if (text.lowercase().contains(needle)) {
                    hints.add(scope.name.lowercase())
                }
            }
        }
        if (hints.isNotEmpty()) {
            results.add(
                SearchResult(
                    filename = item.filename,
                    title = item.title.ifBlank { item.filename },
                    matchHint = hints.distinct().joinToString(", "),
                ),
            )
        }
    }
    return results.sortedBy { searchTitleSortKey(it.title) }
}
