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
import kotlinx.coroutines.launch
import recipejar.search.SearchCatalogEntry
import recipejar.search.SearchResult
import recipejar.search.SearchScope
import recipejar.search.filterRecipesByQuery

/**
 * Search dialog: query + scope chips + clickable results.
 *
 * [initialScopes] pre-selects fields (e.g. Find Titles → titles only).
 * [fieldTextProvider] may hit disk; invoked on a background-friendly coroutine when
 * non-title scopes are active (host should use Dispatchers.IO).
 */
@Composable
fun SearchDialog(
    recipes: List<RecipeListItem>,
    initialScopes: Set<SearchScope> = setOf(SearchScope.TITLES, SearchScope.LABELS),
    fieldTextProvider: suspend () -> Map<String, Map<SearchScope, String>>,
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
    var indexing by remember { mutableStateOf(false) }
    var fieldCache by remember { mutableStateOf<Map<String, Map<SearchScope, String>>?>(null) }
    val scope = rememberCoroutineScope()

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
        val catalog = recipes.map { SearchCatalogEntry(it.filename, it.title) }
        val q = query
        scope.launch {
            val fields = if (needsFields) {
                fieldCache ?: run {
                    indexing = true
                    status = "Indexing…"
                    try {
                        fieldTextProvider().also { fieldCache = it }
                    } finally {
                        indexing = false
                    }
                }
            } else {
                emptyMap()
            }
            val hits = filterRecipesByQuery(catalog, q, scopes, fields)
            results = hits
            status = "${hits.size} match(es)"
        }
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
                    enabled = !indexing,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (!indexing) runSearch() }),
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
                    Button(onClick = { runSearch() }, enabled = !indexing) {
                        Text(if (indexing) "…" else "Search")
                    }
                    if (status != null) {
                        Text(status!!, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                if (results.isEmpty()) {
                    Text(
                        when {
                            indexing -> "Indexing recipes…"
                            status == null -> "Enter a term and press Search"
                            else -> "(no matches)"
                        },
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
