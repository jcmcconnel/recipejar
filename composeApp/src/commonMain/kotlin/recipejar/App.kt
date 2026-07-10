package recipejar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Catalog entry for the alpha-tab index (filename key + display title).
 */
data class RecipeListItem(
    val filename: String,
    val title: String,
)

private val ALPHA_TABS: List<String> = ('A'..'Z').map { it.toString() } + "Other"

/**
 * First-letter bucket matching [FileSystemRecipeRepository] index rules:
 * A–Z from title's first character; everything else → "Other".
 */
internal fun letterBucket(title: String): Char {
    val t = title.trim()
    if (t.isEmpty()) return '0'
    val c = t[0].uppercaseChar()
    return if (c in 'A'..'Z') c else '0'
}

/**
 * Sort key aligned with [FileSystemRecipeRepository] index lists (`lowercase(Locale.US)`).
 * Common code cannot use java.util.Locale; ASCII A–Z fold matches US English for recipe titles.
 */
internal fun titleSortKey(title: String): String = buildString(title.length) {
    for (c in title) {
        append(if (c in 'A'..'Z') c + ('a' - 'A') else c)
    }
}

/**
 * Shell: split layout with alpha-tab index (left) and recipe reader (right).
 *
 * Reader prefers file:// WebView when [webViewReady] is true (KCEF initialized on desktop).
 * Otherwise falls back to scrollable raw HTML text — see [RecipeReader].
 */
@Composable
fun App(
    selectedDir: String?,
    recipes: List<RecipeListItem>,
    selectedFilename: String?,
    selectedFileUrl: String?,
    selectedHtml: String?,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    indexLoading: Boolean = false,
    onOpenRepo: () -> Unit,
    onSelectRecipe: (filename: String) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    // After repo load: keep current tab if it has items; else jump to first non-empty tab.
    LaunchedEffect(selectedDir, recipes) {
        if (selectedDir == null || recipes.isEmpty()) {
            selectedTabIndex = 0
            return@LaunchedEffect
        }
        fun countFor(tab: Int): Int {
            val letter = if (tab in 0..25) ('A' + tab) else '0'
            return recipes.count { letterBucket(it.title) == letter }
        }
        if (countFor(selectedTabIndex) > 0) return@LaunchedEffect
        selectedTabIndex = (0..26).firstOrNull { countFor(it) > 0 } ?: 0
    }

    val selectedLetter: Char =
        if (selectedTabIndex in 0..25) ('A' + selectedTabIndex) else '0'

    val filtered = remember(recipes, selectedLetter) {
        recipes
            .filter { letterBucket(it.title) == selectedLetter }
            .sortedBy { titleSortKey(it.title) }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "RecipeJar",
                    style = MaterialTheme.typography.titleLarge,
                )
                Button(onClick = onOpenRepo) {
                    Text("Open repository")
                }
                if (selectedDir != null) {
                    Text(
                        selectedDir,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        if (indexLoading) "Loading…" else "${recipes.size} recipes",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (restartRequired) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "WebView installed — restart RecipeJar to enable rendered recipes.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            HorizontalDivider()

            if (selectedDir == null) {
                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No repository selected. Open a directory containing recipes (e.g. Test/Recipes).",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else if (indexLoading && recipes.isEmpty()) {
                Box(
                    Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Loading recipes…",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                // Split: index | reader
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    // Left: alpha index
                    Column(
                        Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                            .padding(8.dp),
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            edgePadding = 4.dp,
                        ) {
                            ALPHA_TABS.forEachIndexed { index, label ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (selectedLetter == '0') "Other" else "Letter $selectedLetter",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            "${filtered.size} recipe(s)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        if (filtered.isEmpty()) {
                            Text(
                                "(none)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                            )
                        } else {
                            LazyColumn(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                            ) {
                                items(filtered, key = { it.filename }) { item ->
                                    val selected = item.filename == selectedFilename
                                    Surface(
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectRecipe(item.filename) },
                                    ) {
                                        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                            Text(
                                                item.title.ifBlank { item.filename },
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                item.filename,
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

                    VerticalDivider()

                    // Right: reader
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(8.dp),
                    ) {
                        RecipeReader(
                            selectedFilename = selectedFilename,
                            selectedFileUrl = selectedFileUrl,
                            selectedHtml = selectedHtml,
                            webViewReady = webViewReady,
                            restartRequired = restartRequired,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Read-only recipe view.
 *
 * Preferred path: compose-webview-multiplatform [WebView] loading `file://…` so relative
 * CSS (`style/default.css`) and images resolve against the repository directory.
 *
 * Fallback: scrollable monospaced HTML source. Used when KCEF has not finished initializing
 * (first-run CEF download / install can fail or require restart). Desktop WebView backend is
 * KCEF-based; without a ready CEF runtime, embedding WebView crashes or shows nothing.
 * See Main.kt KCEF bootstrap and README notes in the commit message / summary.
 */
@Composable
fun RecipeReader(
    selectedFilename: String?,
    selectedFileUrl: String?,
    selectedHtml: String?,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (selectedFilename == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Select a recipe from the index",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(modifier) {
        Text(
            selectedFilename,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        // WebView path is provided as an optional composable from desktop when ready.
        // Common shell uses HTML text fallback; desktop Main can overlay WebView via
        // [RecipeHtmlWebView] expect/actual (desktop).
        if (webViewReady && !selectedFileUrl.isNullOrBlank()) {
            RecipeHtmlWebView(
                fileUrl = selectedFileUrl,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else if (selectedHtml != null) {
            val scroll = rememberScrollState()
            if (!webViewReady) {
                val banner = if (restartRequired) {
                    "Showing HTML source — restart RecipeJar after WebView install to enable rendered view."
                } else {
                    "Showing HTML source (WebView/KCEF not ready — CSS may not apply)."
                }
                Text(
                    banner,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text = selectedHtml,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(4.dp),
            )
        } else {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Unable to load recipe content.")
            }
        }
    }
}

/**
 * Platform WebView host for a local `file://` recipe URL.
 * Desktop: compose-webview-multiplatform when KCEF is initialized.
 * Default/common: empty — App falls through only when [webViewReady] is true,
 * so desktop must provide a real implementation.
 */
@Composable
expect fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier = Modifier,
)
