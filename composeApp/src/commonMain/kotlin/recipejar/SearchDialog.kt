package recipejar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Which recipe fields participate in a search.
 * Basic scope: titles + labels (plan PR-8). Notes/ingredients/procedure optional extras.
 */
enum class SearchScope {
    TITLES,
    LABELS,
    NOTES,
    INGREDIENTS,
    PROCEDURE,
}

/**
 * One hit from [filterRecipesByQuery].
 */
data class SearchResult(
    val filename: String,
    val title: String,
    val matchHint: String,
)

/**
 * Substring (case-insensitive) filter over catalog + optional field text.
 *
 * [fieldText] maps filename → combined searchable text for labels/notes/etc.
 * Title always comes from [recipes].
 */
fun filterRecipesByQuery(
    recipes: List<RecipeListItem>,
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
    return results.sortedBy { titleSortKey(it.title) }
}

/**
 * Search dialog: query + scope checkboxes + clickable results.
 *
 * [initialScopes] pre-selects fields (e.g. Find Titles → titles only).
 * [fieldTextProvider] supplies non-title field text keyed by filename (called once when searching).
 */
@Composable
fun SearchDialog(
    recipes: List<RecipeListItem>,
    initialScopes: Set<SearchScope> = setOf(SearchScope.TITLES, SearchScope.LABELS),
    fieldTextProvider: () -> Map<String, Map<SearchScope, String>>,
    onSelect: (filename: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var inTitles by remember { mutableStateOf(SearchScope.TITLES in initialScopes) }
    var inLabels by remember { mutableStateOf(SearchScope.LABELS in initialScopes) }
    var inNotes by remember { mutableStateOf(SearchScope.NOTES in initialScopes) }
    var inIngredients by remember { mutableStateOf(SearchScope.INGREDIENTS in initialScopes) }
    var inProcedure by remember { mutableStateOf(SearchScope.PROCEDURE in initialScopes) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var status by remember { mutableStateOf<String?>(null) }
    var fieldCache by remember { mutableStateOf<Map<String, Map<SearchScope, String>>?>(null) }

    fun currentScopes(): Set<SearchScope> = buildSet {
        if (inTitles) add(SearchScope.TITLES)
        if (inLabels) add(SearchScope.LABELS)
        if (inNotes) add(SearchScope.NOTES)
        if (inIngredients) add(SearchScope.INGREDIENTS)
        if (inProcedure) add(SearchScope.PROCEDURE)
    }

    fun runSearch() {
        val scopes = currentScopes()
        if (query.isBlank()) {
            results = emptyList()
            status = "Enter a search term"
            return
        }
        if (scopes.isEmpty()) {
            results = emptyList()
            status = "Select at least one field"
            return
        }
        val needsFields = scopes.any { it != SearchScope.TITLES }
        val fields = if (needsFields) {
            fieldCache ?: fieldTextProvider().also { fieldCache = it }
        } else {
            emptyMap()
        }
        results = filterRecipesByQuery(recipes, query, scopes, fields)
        status = "${results.size} match(es)"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 420.dp, max = 560.dp)
                .heightIn(min = 360.dp, max = 520.dp)
                .padding(16.dp),
        ) {
            Column(Modifier.padding(16.dp).fillMaxSize()) {
                Text("Search recipes", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Find") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { runSearch() }),
                )
                Spacer(Modifier.height(8.dp))
                Text("Search in:", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(selected = inTitles, onClick = { inTitles = !inTitles }, label = { Text("Titles") })
                    FilterChip(selected = inLabels, onClick = { inLabels = !inLabels }, label = { Text("Labels") })
                    FilterChip(selected = inNotes, onClick = { inNotes = !inNotes }, label = { Text("Notes") })
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterChip(
                        selected = inIngredients,
                        onClick = { inIngredients = !inIngredients },
                        label = { Text("Ingredients") },
                    )
                    FilterChip(
                        selected = inProcedure,
                        onClick = { inProcedure = !inProcedure },
                        label = { Text("Procedure") },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { runSearch() }) { Text("Search") }
                    if (status != null) {
                        Text(status!!, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (results.isEmpty()) {
                    Text(
                        if (status == null) "Enter a term and press Search" else "(no matches)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                        items(results, key = { it.filename }) { hit ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(hit.filename)
                                        onDismiss()
                                    },
                            ) {
                                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    Text(
                                        hit.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${hit.filename} · ${hit.matchHint}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
