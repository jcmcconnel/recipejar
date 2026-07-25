package recipejar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/** Width below which the shell uses single-pane (index OR reader) navigation. */
internal val COMPACT_WIDTH_BREAKPOINT = 600.dp

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
 * Shell: split layout with Rolodex-edge alpha index (left) and content pane (right).
 *
 * Content pane swaps by [isEditing]: structured [RecipeFormEditor] vs [RecipeReader].
 * Reader prefers file:// WebView when [webViewReady] is true (KCEF initialized on desktop).
 * Otherwise falls back to scrollable raw HTML text — see [RecipeReader].
 *
 * [materialMenus]: when non-null, render Material dropdown menus in the top bar (hybrid path;
 * macOS keeps the native [androidx.compose.ui.window.MenuBar] instead).
 *
 * Compact layout: window width under [COMPACT_WIDTH_BREAKPOINT], or [forceCompactLayout],
 * uses single-pane navigation (index list ↔ reader) for mobile-style testing on desktop.
 */
@Composable
fun App(
    selectedDir: String?,
    recipes: List<RecipeListItem>,
    selectedFilename: String?,
    selectedFileUrl: String?,
    selectedHtml: String?,
    editingRecipe: recipejar.domain.Recipe? = null,
    knownLabels: List<String> = emptyList(),
    /** Unit plurals from units.txt (no blank entry — form adds it). */
    unitCatalog: List<String> = emptyList(),
    /** Bundled welcome page HTML; shown when no recipe is selected. */
    welcomeHtml: String = "",
    /** file:// URL for welcome (WebView); null uses HTML text fallback. */
    welcomeFileUrl: String? = null,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    webViewStatusText: String? = null,
    indexLoading: Boolean = false,
    isEditing: Boolean = false,
    statusMessage: String? = null,
    materialMenus: AppMenuModel? = null,
    forceCompactLayout: Boolean = false,
    onForceCompactChange: ((Boolean) -> Unit)? = null,
    onOpenRepo: () -> Unit,
    onSelectRecipe: (filename: String) -> Unit,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit = {},
    onEditFocusSection: (RecipeEditSection) -> Unit = {},
    onClearSelection: () -> Unit = {},
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    /** Compact single-pane: false = index, true = recipe content. */
    var compactShowContent by remember { mutableStateOf(false) }

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

    // After selection / title-rename save: jump alpha tab to the selected recipe's letter.
    // Keyed only on selectedFilename so manual tab browsing is not forced back on index refresh.
    LaunchedEffect(selectedFilename) {
        if (selectedFilename == null) {
            compactShowContent = false
            return@LaunchedEffect
        }
        if (recipes.isEmpty()) return@LaunchedEffect
        val item = recipes.find { it.filename == selectedFilename } ?: return@LaunchedEffect
        val letter = letterBucket(item.title)
        selectedTabIndex = if (letter == '0') 26 else (letter - 'A')
        compactShowContent = true
    }

    val selectedLetter: Char =
        if (selectedTabIndex in 0..25) ('A' + selectedTabIndex) else '0'

    val filtered = remember(recipes, selectedLetter) {
        recipes
            .filter { letterBucket(it.title) == selectedLetter }
            .sortedBy { titleSortKey(it.title) }
    }

    val letterCounts = remember(recipes) {
        IntArray(27) { tab ->
            val letter = if (tab in 0..25) ('A' + tab) else '0'
            recipes.count { letterBucket(it.title) == letter }
        }
    }

    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                selectedDir = selectedDir,
                recipeCount = recipes.size,
                indexLoading = indexLoading,
                isEditing = isEditing,
                selectedFilename = selectedFilename,
                materialMenus = materialMenus,
                forceCompactLayout = forceCompactLayout,
                onForceCompactChange = onForceCompactChange,
                onOpenRepo = onOpenRepo,
            )

            if (restartRequired || webViewStatusText != null) {
                val banner = when {
                    restartRequired ->
                        "WebView installed — restart RecipeJar to enable rendered recipes."
                    webViewStatusText != null -> webViewStatusText
                    else -> null
                }
                if (banner != null) {
                    Surface(
                        color = if (restartRequired) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            banner,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            if (statusMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            HorizontalDivider()

            if (selectedDir == null) {
                // No repo yet: still show welcome (not a blank/black pane) + open CTA.
                Column(Modifier.weight(1f).fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "No repository open.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = onOpenRepo) {
                            Text("Open repository")
                        }
                    }
                    HorizontalDivider()
                    WelcomePane(
                        welcomeHtml = welcomeHtml,
                        welcomeFileUrl = welcomeFileUrl,
                        webViewReady = webViewReady,
                        restartRequired = restartRequired,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
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
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val compact = forceCompactLayout || maxWidth < COMPACT_WIDTH_BREAKPOINT
                    if (compact) {
                        CompactShell(
                            compactShowContent = compactShowContent,
                            onBackToIndex = {
                                compactShowContent = false
                                onClearSelection()
                            },
                            selectedTabIndex = selectedTabIndex,
                            onSelectTab = { selectedTabIndex = it },
                            letterCounts = letterCounts,
                            selectedLetter = selectedLetter,
                            filtered = filtered,
                            selectedFilename = selectedFilename,
                            onSelectRecipe = { name ->
                                onSelectRecipe(name)
                                compactShowContent = true
                            },
                            isEditing = isEditing,
                            selectedHtml = selectedHtml,
                            editingRecipe = editingRecipe,
                            knownLabels = knownLabels,
                            unitCatalog = unitCatalog,
                            selectedFileUrl = selectedFileUrl,
                            welcomeHtml = welcomeHtml,
                            welcomeFileUrl = welcomeFileUrl,
                            webViewReady = webViewReady,
                            restartRequired = restartRequired,
                            onRecipeChange = onRecipeChange,
                            onEditFocusSection = onEditFocusSection,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        WideShell(
                            selectedTabIndex = selectedTabIndex,
                            onSelectTab = { selectedTabIndex = it },
                            letterCounts = letterCounts,
                            selectedLetter = selectedLetter,
                            filtered = filtered,
                            selectedFilename = selectedFilename,
                            onSelectRecipe = onSelectRecipe,
                            isEditing = isEditing,
                            selectedHtml = selectedHtml,
                            editingRecipe = editingRecipe,
                            knownLabels = knownLabels,
                            unitCatalog = unitCatalog,
                            selectedFileUrl = selectedFileUrl,
                            welcomeHtml = welcomeHtml,
                            welcomeFileUrl = welcomeFileUrl,
                            webViewReady = webViewReady,
                            restartRequired = restartRequired,
                            onRecipeChange = onRecipeChange,
                            onEditFocusSection = onEditFocusSection,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTopBar(
    selectedDir: String?,
    recipeCount: Int,
    indexLoading: Boolean,
    isEditing: Boolean,
    selectedFilename: String?,
    materialMenus: AppMenuModel?,
    forceCompactLayout: Boolean,
    onForceCompactChange: ((Boolean) -> Unit)?,
    onOpenRepo: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        if (materialMenus != null) {
            MaterialMenuBar(model = materialMenus)
            HorizontalDivider()
        }
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
            // When Material menus include Recipe → actions, Open is still convenient here.
            if (materialMenus == null) {
                Button(onClick = onOpenRepo) {
                    Text("Open repository")
                }
            } else {
                OutlinedButton(onClick = onOpenRepo) {
                    Text("Open repository")
                }
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
                    if (indexLoading) "Loading…" else "$recipeCount recipes",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (selectedFilename != null) {
                    Text(
                        if (isEditing) "[editing]" else "[read]",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (onForceCompactChange != null) {
                FilterChip(
                    selected = forceCompactLayout,
                    onClick = { onForceCompactChange(!forceCompactLayout) },
                    label = { Text("Phone layout") },
                )
            }
        }
    }
}

/**
 * Material3 dropdown menu strip for hybrid menu mode (non-macOS).
 */
@Composable
fun MaterialMenuBar(
    model: AppMenuModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        model.menus.forEach { menu ->
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(menu.title, style = MaterialTheme.typography.labelLarge)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    menu.entries.forEach { entry ->
                        when (entry) {
                            is AppMenuEntry.Separator -> HorizontalDivider()
                            is AppMenuEntry.Item -> {
                                DropdownMenuItem(
                                    text = { Text(entry.title) },
                                    onClick = {
                                        expanded = false
                                        entry.onClick()
                                    },
                                    enabled = entry.enabled,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Side-by-side: Rolodex letter rail + recipe list | reader/editor.
 * Letter placement mirrors Swing [javax.swing.JTabbedPane.LEFT].
 */
@Composable
private fun WideShell(
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    letterCounts: IntArray,
    selectedLetter: Char,
    filtered: List<RecipeListItem>,
    selectedFilename: String?,
    onSelectRecipe: (String) -> Unit,
    isEditing: Boolean,
    selectedHtml: String?,
    editingRecipe: recipejar.domain.Recipe?,
    knownLabels: List<String>,
    unitCatalog: List<String>,
    selectedFileUrl: String?,
    welcomeHtml: String,
    welcomeFileUrl: String?,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        IndexPane(
            selectedTabIndex = selectedTabIndex,
            onSelectTab = onSelectTab,
            letterCounts = letterCounts,
            selectedLetter = selectedLetter,
            filtered = filtered,
            selectedFilename = selectedFilename,
            onSelectRecipe = onSelectRecipe,
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight()
                .padding(8.dp),
        )
        VerticalDivider()
        ContentPane(
            selectedFilename = selectedFilename,
            isEditing = isEditing,
            selectedHtml = selectedHtml,
            editingRecipe = editingRecipe,
            knownLabels = knownLabels,
            unitCatalog = unitCatalog,
            selectedFileUrl = selectedFileUrl,
            welcomeHtml = welcomeHtml,
            welcomeFileUrl = welcomeFileUrl,
            webViewReady = webViewReady,
            restartRequired = restartRequired,
            onRecipeChange = onRecipeChange,
            onEditFocusSection = onEditFocusSection,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp),
        )
    }
}

/**
 * Single-pane mobile-style shell: index XOR content with back control.
 */
@Composable
private fun CompactShell(
    compactShowContent: Boolean,
    onBackToIndex: () -> Unit,
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    letterCounts: IntArray,
    selectedLetter: Char,
    filtered: List<RecipeListItem>,
    selectedFilename: String?,
    onSelectRecipe: (String) -> Unit,
    isEditing: Boolean,
    selectedHtml: String?,
    editingRecipe: recipejar.domain.Recipe?,
    knownLabels: List<String>,
    unitCatalog: List<String>,
    selectedFileUrl: String?,
    welcomeHtml: String,
    welcomeFileUrl: String?,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (compactShowContent && selectedFilename != null) {
        Column(modifier.padding(8.dp)) {
            TextButton(onClick = onBackToIndex) {
                Text("← Index")
            }
            ContentPane(
                selectedFilename = selectedFilename,
                isEditing = isEditing,
                selectedHtml = selectedHtml,
                editingRecipe = editingRecipe,
                knownLabels = knownLabels,
                unitCatalog = unitCatalog,
                selectedFileUrl = selectedFileUrl,
                welcomeHtml = welcomeHtml,
                welcomeFileUrl = welcomeFileUrl,
                webViewReady = webViewReady,
                restartRequired = restartRequired,
                onRecipeChange = onRecipeChange,
                onEditFocusSection = onEditFocusSection,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    } else {
        IndexPane(
            selectedTabIndex = selectedTabIndex,
            onSelectTab = onSelectTab,
            letterCounts = letterCounts,
            selectedLetter = selectedLetter,
            filtered = filtered,
            selectedFilename = selectedFilename,
            onSelectRecipe = onSelectRecipe,
            modifier = modifier.padding(8.dp),
        )
    }
}

@Composable
private fun IndexPane(
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    letterCounts: IntArray,
    selectedLetter: Char,
    filtered: List<RecipeListItem>,
    selectedFilename: String?,
    onSelectRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        // Rolodex edge: vertical A–Z rail (Swing JTabbedPane.LEFT equivalent)
        LetterRail(
            selectedTabIndex = selectedTabIndex,
            onSelectTab = onSelectTab,
            letterCounts = letterCounts,
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f).fillMaxHeight()) {
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
    }
}

/**
 * Vertical A–Z (+ Other) letter strip — the Rolodex edge.
 * Empty letters are dimmed but still selectable (browse empty buckets).
 */
@Composable
private fun LetterRail(
    selectedTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    letterCounts: IntArray,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedTabIndex) {
        listState.animateScrollToItem(selectedTabIndex.coerceIn(0, ALPHA_TABS.lastIndex))
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
        tonalElevation = 1.dp,
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(ALPHA_TABS.size) { index ->
                val label = ALPHA_TABS[index]
                val selected = index == selectedTabIndex
                val count = letterCounts.getOrElse(index) { 0 }
                val empty = count == 0
                val bg = when {
                    selected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val fg = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    empty -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTab(index) }
                        .background(bg)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (label == "Other") "#" else label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = fg,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentPane(
    selectedFilename: String?,
    isEditing: Boolean,
    selectedHtml: String?,
    editingRecipe: recipejar.domain.Recipe?,
    knownLabels: List<String>,
    unitCatalog: List<String>,
    selectedFileUrl: String?,
    welcomeHtml: String,
    welcomeFileUrl: String?,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (isEditing) {
            if (editingRecipe == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Select a recipe from the index, or File → New",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                RecipeFormEditor(
                    recipe = editingRecipe,
                    knownLabels = knownLabels,
                    unitCatalog = unitCatalog,
                    onRecipeChange = onRecipeChange,
                    onFocusSection = onEditFocusSection,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            RecipeReader(
                selectedFilename = selectedFilename,
                selectedFileUrl = selectedFileUrl,
                selectedHtml = selectedHtml,
                welcomeHtml = welcomeHtml,
                welcomeFileUrl = welcomeFileUrl,
                webViewReady = webViewReady,
                restartRequired = restartRequired,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Bundled welcome page when no recipe is selected (avoids blank/black CEF pane).
 */
@Composable
fun WelcomePane(
    welcomeHtml: String,
    welcomeFileUrl: String?,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            "Welcome",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        HorizontalDivider()
        Spacer(Modifier.height(4.dp))
        if (webViewReady && !welcomeFileUrl.isNullOrBlank()) {
            RecipeHtmlWebView(
                fileUrl = welcomeFileUrl,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else {
            val scroll = rememberScrollState()
            if (!webViewReady) {
                Text(
                    if (restartRequired) {
                        "WebView not ready — restart after install for rendered view."
                    } else {
                        "WebView not ready — showing welcome text."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            val display = welcomeHtml.ifBlank {
                "<p>Welcome to RecipeJar. Open a repository and select a recipe to begin.</p>"
            }
            Text(
                text = stripSimpleHtml(display),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(8.dp),
            )
        }
    }
}

/** Minimal HTML → plain text for welcome fallback (no full HTML engine). */
internal fun stripSimpleHtml(html: String): String {
    return html
        .replace(Regex("(?is)<script[^>]*>.*?</script>"), "")
        .replace(Regex("(?is)<style[^>]*>.*?</style>"), "")
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</p\\s*>"), "\n\n")
        .replace(Regex("(?i)</h[1-6]\\s*>"), "\n")
        .replace(Regex("(?i)</li\\s*>"), "\n")
        .replace(Regex("(?i)<li[^>]*>"), "• ")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("[ \t]+\n"), "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

/**
 * Read-only recipe view, or [WelcomePane] when nothing is selected.
 *
 * Preferred path: compose-webview-multiplatform [WebView] loading `file://…` so relative
 * CSS (`style/default.css`) and images resolve against the repository directory.
 *
 * Fallback: scrollable HTML source. Used when KCEF has not finished initializing.
 */
@Composable
fun RecipeReader(
    selectedFilename: String?,
    selectedFileUrl: String?,
    selectedHtml: String?,
    welcomeHtml: String = "",
    welcomeFileUrl: String? = null,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (selectedFilename == null) {
        WelcomePane(
            welcomeHtml = welcomeHtml,
            welcomeFileUrl = welcomeFileUrl,
            webViewReady = webViewReady,
            restartRequired = restartRequired,
            modifier = modifier,
        )
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

        if (webViewReady && !selectedFileUrl.isNullOrBlank()) {
            RecipeHtmlWebView(
                fileUrl = selectedFileUrl,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        } else if (selectedHtml != null) {
            val scroll = rememberScrollState()
            val banner = when {
                !webViewReady && restartRequired ->
                    "Showing HTML source — restart RecipeJar after WebView install to enable rendered view."
                !webViewReady ->
                    "Showing HTML source (WebView/KCEF not ready — CSS may not apply)."
                selectedFileUrl.isNullOrBlank() ->
                    "Showing unsaved buffer (save to refresh rendered WebView preview)."
                else -> null
            }
            if (banner != null) {
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
            // Loading or missing file: never leave a blank black WebView surface.
            WelcomePane(
                welcomeHtml = welcomeHtml,
                welcomeFileUrl = welcomeFileUrl,
                webViewReady = webViewReady,
                restartRequired = restartRequired,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
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
