package recipejar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import recipejar.html.CategoryNavigation

/**
 * Catalog entry for the alpha-tab index (filename key + display title + labels).
 * [labels] power category → listing navigation from the program-footer.
 */
data class RecipeListItem(
    val filename: String,
    val title: String,
    val labels: List<String> = emptyList(),
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
    /**
     * When false, welcome never uses WebView (text only). Set false while desktop
     * dialogs/modals are open so KCEF cannot paint over Preferences etc.
     */
    welcomeWebViewEnabled: Boolean = true,
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
    /**
     * When non-null, replaces the main content pane (mobile/compact modals).
     * Desktop hosts typically pass null and use [Dialog] overlays instead.
     */
    contentModal: (@Composable () -> Unit)? = null,
    /** Persisted appearance id (forest/ocean/slate/warm/rose). */
    appearanceId: String = AppearanceTheme.DEFAULT_ID,
    appearanceDark: Boolean = false,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    /** Compact single-pane: false = index, true = recipe content. */
    var compactShowContent by remember { mutableStateOf(false) }
    /**
     * When set (program-footer category activation), the index lists recipes that carry
     * this label rather than filtering solely by title letter.
     */
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    fun activateCategory(label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return
        categoryFilter = trimmed
        selectedTabIndex = CategoryNavigation.tabIndexForCategory(trimmed)
        compactShowContent = false
        onClearSelection()
    }

    // After repo load: keep current tab if it has items; else jump to first non-empty tab.
    LaunchedEffect(selectedDir, recipes) {
        if (selectedDir == null || recipes.isEmpty()) {
            selectedTabIndex = 0
            categoryFilter = null
            return@LaunchedEffect
        }
        if (categoryFilter != null) return@LaunchedEffect
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
        // Selecting a recipe clears category filter so title letter navigation wins.
        categoryFilter = null
        val item = recipes.find { it.filename == selectedFilename } ?: return@LaunchedEffect
        val letter = letterBucket(item.title)
        selectedTabIndex = if (letter == '0') 26 else (letter - 'A')
        compactShowContent = true
    }

    val selectedLetter: Char =
        if (selectedTabIndex in 0..25) ('A' + selectedTabIndex) else '0'

    val filtered = remember(recipes, selectedLetter, categoryFilter) {
        val base = if (categoryFilter != null) {
            val cat = categoryFilter!!
            recipes.filter { item ->
                item.labels.any { it.equals(cat, ignoreCase = true) }
            }
        } else {
            recipes.filter { letterBucket(it.title) == selectedLetter }
        }
        base.sortedBy { titleSortKey(it.title) }
    }

    val letterCounts = remember(recipes) {
        IntArray(27) { tab ->
            val letter = if (tab in 0..25) ('A' + tab) else '0'
            recipes.count { letterBucket(it.title) == letter }
        }
    }

    MaterialTheme(colorScheme = AppearanceTheme.schemeFor(appearanceId, appearanceDark)) {
        Column(Modifier.fillMaxSize()) {
            // Menu strip only (Material path). Dense header band removed — Open repository
            // lives under Recipe → Open repository. Native macOS uses the screen menu bar.
            if (materialMenus != null) {
                MaterialMenuBar(model = materialMenus)
                HorizontalDivider()
            }

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

            // Content modals (mobile) replace the whole body — never leave welcome/WebView under them.
            if (contentModal != null) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    contentModal()
                }
            } else if (selectedDir == null) {
                // No repo yet: open CTA + welcome (WebView only when [welcomeWebViewEnabled]).
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
                        webViewReady = webViewReady && welcomeWebViewEnabled,
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
                            onSelectTab = {
                                categoryFilter = null
                                selectedTabIndex = it
                            },
                            letterCounts = letterCounts,
                            selectedLetter = selectedLetter,
                            filtered = filtered,
                            categoryFilter = categoryFilter,
                            recipes = recipes,
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
                            welcomeWebViewEnabled = welcomeWebViewEnabled,
                            webViewReady = webViewReady,
                            restartRequired = restartRequired,
                            onRecipeChange = onRecipeChange,
                            onEditFocusSection = onEditFocusSection,
                            onCategoryActivate = { activateCategory(it) },
                            onClearCategoryFilter = { categoryFilter = null },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        WideShell(
                            selectedTabIndex = selectedTabIndex,
                            onSelectTab = {
                                categoryFilter = null
                                selectedTabIndex = it
                            },
                            letterCounts = letterCounts,
                            selectedLetter = selectedLetter,
                            filtered = filtered,
                            categoryFilter = categoryFilter,
                            recipes = recipes,
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
                            welcomeWebViewEnabled = welcomeWebViewEnabled,
                            webViewReady = webViewReady,
                            restartRequired = restartRequired,
                            onRecipeChange = onRecipeChange,
                            onEditFocusSection = onEditFocusSection,
                            onCategoryActivate = { activateCategory(it) },
                            onClearCategoryFilter = { categoryFilter = null },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material3 dropdown menu strip for hybrid menu mode (non-macOS).
 * The dense info header (app name / path / count / mode / Phone) was removed;
 * open a repository via Recipe → Open repository.
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
    categoryFilter: String?,
    recipes: List<RecipeListItem>,
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
    welcomeWebViewEnabled: Boolean,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    onCategoryActivate: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        IndexPane(
            selectedTabIndex = selectedTabIndex,
            onSelectTab = onSelectTab,
            letterCounts = letterCounts,
            selectedLetter = selectedLetter,
            filtered = filtered,
            categoryFilter = categoryFilter,
            recipes = recipes,
            selectedFilename = selectedFilename,
            onSelectRecipe = onSelectRecipe,
            onCategoryActivate = onCategoryActivate,
            onClearCategoryFilter = onClearCategoryFilter,
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
            welcomeWebViewEnabled = welcomeWebViewEnabled,
            webViewReady = webViewReady,
            restartRequired = restartRequired,
            onRecipeChange = onRecipeChange,
            onEditFocusSection = onEditFocusSection,
            onCategoryActivate = onCategoryActivate,
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
    categoryFilter: String?,
    recipes: List<RecipeListItem>,
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
    welcomeWebViewEnabled: Boolean,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    onCategoryActivate: (String) -> Unit,
    onClearCategoryFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Compact content must get a *bounded height* (fillMaxSize → Column → weight).
    if (compactShowContent && selectedFilename != null) {
        Column(
            modifier
                .fillMaxSize()
                .padding(8.dp),
        ) {
            TextButton(
                onClick = onBackToIndex,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
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
                welcomeWebViewEnabled = welcomeWebViewEnabled,
                webViewReady = webViewReady,
                restartRequired = restartRequired,
                onRecipeChange = onRecipeChange,
                onEditFocusSection = onEditFocusSection,
                onCategoryActivate = onCategoryActivate,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight(),
            )
        }
    } else {
        IndexPane(
            selectedTabIndex = selectedTabIndex,
            onSelectTab = onSelectTab,
            letterCounts = letterCounts,
            selectedLetter = selectedLetter,
            filtered = filtered,
            categoryFilter = categoryFilter,
            recipes = recipes,
            selectedFilename = selectedFilename,
            onSelectRecipe = onSelectRecipe,
            onCategoryActivate = onCategoryActivate,
            onClearCategoryFilter = onClearCategoryFilter,
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp),
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
    categoryFilter: String? = null,
    recipes: List<RecipeListItem> = emptyList(),
    selectedFilename: String?,
    onSelectRecipe: (String) -> Unit,
    onCategoryActivate: (String) -> Unit = {},
    onClearCategoryFilter: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val letterCategories = remember(recipes, selectedLetter, categoryFilter) {
        if (categoryFilter != null) {
            emptyList()
        } else {
            CategoryIndexLogic.categoriesForLetter(recipes, selectedLetter)
        }
    }
    Row(modifier) {
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
                when {
                    categoryFilter != null -> "Category: $categoryFilter"
                    selectedLetter == '0' -> "Other"
                    else -> "Letter $selectedLetter"
                },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            if (categoryFilter != null) {
                TextButton(
                    onClick = onClearCategoryFilter,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) {
                    Text("← All recipes", style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(
                "${filtered.size} recipe(s)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                // Category discovery without opening a recipe (classic alphatab sub-lists).
                if (categoryFilter == null && letterCategories.isNotEmpty()) {
                    item(key = "cat-header") {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                    items(letterCategories, key = { "cat-$it" }) { cat ->
                        val count = CategoryIndexLogic.recipesForCategory(recipes, cat).size
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryActivate(cat) },
                        ) {
                            Text(
                                "$cat ($count)",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            )
                        }
                    }
                    item(key = "recipes-header") {
                        Text(
                            "Recipes",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
                if (filtered.isEmpty()) {
                    item(key = "none") {
                        Text(
                            "(none)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                } else {
                    items(filtered, key = { it.filename }) { item ->
                        val selected = item.filename == selectedFilename
                        val displayTitle = item.title.trim().ifBlank {
                            item.filename.removeSuffix(".html").removeSuffix(".HTML")
                                .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                        }
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
                            Text(
                                displayTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            )
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
    welcomeWebViewEnabled: Boolean = true,
    webViewReady: Boolean,
    restartRequired: Boolean,
    onRecipeChange: (recipejar.domain.Recipe) -> Unit,
    onEditFocusSection: (RecipeEditSection) -> Unit,
    onCategoryActivate: (String) -> Unit = {},
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
                welcomeWebViewEnabled = welcomeWebViewEnabled,
                webViewReady = webViewReady,
                restartRequired = restartRequired,
                onCategoryActivate = onCategoryActivate,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Body fragment of a welcome HTML document (or the raw string if there is no
 * `<body>`). Same input the read-only recipe notes/procedure path consumes.
 */
internal fun welcomeHtmlForReadonly(html: String): String {
    val raw = html.ifBlank {
        "<h1>RecipeJar</h1><p>Welcome to RecipeJar. Open a repository and select a recipe to begin.</p>"
    }
    val withoutComments = raw.replace(Regex("(?is)<!--.*?-->"), "")
    val body = Regex("(?is)<body[^>]*>(.*)</body>").find(withoutComments)?.groupValues?.get(1)
        ?: withoutComments
    return body.trim()
}

/**
 * Welcome when no recipe is selected — Compose styled-fragment look, matching
 * [RecipeReadonlyDocument] (not a WebView HTML page, not tag-stripped dump).
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun WelcomePane(
    welcomeHtml: String,
    welcomeFileUrl: String?,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    welcomeWebViewEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val fragment = remember(welcomeHtml) { welcomeHtmlForReadonly(welcomeHtml) }
    val styled = remember(fragment) { htmlFragmentToAnnotatedString(fragment) }
    val scroll = rememberScrollState()
    Column(modifier.fillMaxSize()) {
        Text(
            "Welcome",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 2.dp),
        )
        HorizontalDivider()
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                ContentScrollLayout.contentScrollSurface(Modifier, scroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = styled,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(24.dp))
            }
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
        .replace("&deg;", "°")
        .replace(Regex("[ \t]+\n"), "\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}

/**
 * Reader fallback when WebView is unavailable: structured plain text from core
 * recipe sections so the pane is never blank monospaced source.
 */
internal fun recipePlainTextFromHtml(html: String): String {
    fun section(id: String): String {
        val re = Regex(
            """(?is)<div[^>]*id\s*=\s*["']$id["'][^>]*>(.*?)</div>""",
        )
        val inner = re.find(html)?.groupValues?.get(1) ?: return ""
        return stripSimpleHtml(inner)
    }
    val title = Regex("(?is)<title>(.*?)</title>").find(html)?.groupValues?.get(1)?.trim()
        ?: Regex("(?is)<h1>(.*?)</h1>").find(html)?.groupValues?.get(1)?.let { stripSimpleHtml(it) }
        ?: ""
    val notes = section("notes")
    val ingredients = section("ingredients")
    val procedure = section("procedure")
    return buildString {
        if (title.isNotBlank()) {
            appendLine(title)
            appendLine()
        }
        if (notes.isNotBlank()) {
            appendLine("Notes:")
            appendLine(notes)
            appendLine()
        }
        if (ingredients.isNotBlank()) {
            appendLine("You will need:")
            appendLine(ingredients)
            appendLine()
        }
        if (procedure.isNotBlank()) {
            appendLine("Procedure:")
            appendLine(procedure)
        }
    }.trim().ifBlank { stripSimpleHtml(html) }
}

/**
 * Read-only recipe view, or [WelcomePane] when nothing is selected.
 *
 * Recipe body uses [RecipeReadonlyDocument]: a Compose-rendered view of the parsed
 * recipe (title, notes, ingredients, procedure). This is **not** HTML source and does
 * not depend on KCEF/WebView sizing — so Desktop wide and Phone/compact both always
 * show content. Category chips provide program-footer-style navigation.
 *
 * Welcome uses the same [htmlFragmentToAnnotatedString] path as notes/procedure.
 */
@Composable
fun RecipeReader(
    selectedFilename: String?,
    selectedFileUrl: String?,
    selectedHtml: String?,
    welcomeHtml: String = "",
    welcomeFileUrl: String? = null,
    welcomeWebViewEnabled: Boolean = true,
    webViewReady: Boolean,
    restartRequired: Boolean = false,
    onCategoryActivate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (selectedFilename == null) {
        WelcomePane(
            welcomeHtml = welcomeHtml,
            welcomeFileUrl = welcomeFileUrl,
            webViewReady = webViewReady,
            restartRequired = restartRequired,
            welcomeWebViewEnabled = welcomeWebViewEnabled,
            modifier = modifier,
        )
        return
    }

    val categoryLabels = remember(selectedHtml) { RecipePreviewHtml.labelsFromHtml(selectedHtml) }
    val headerTitle = remember(selectedHtml, selectedFilename) {
        titleFromRecipeHtml(selectedHtml).ifBlank {
            selectedFilename.removeSuffix(".html").removeSuffix(".HTML")
        }
    }

    // Header (intrinsic) + scrollable body viewport (weight) — works in compact and wide.
    Column(modifier.fillMaxSize()) {
        Text(
            headerTitle,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        HorizontalDivider()
        if (categoryLabels.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Categories:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                categoryLabels.forEach { label ->
                    TextButton(
                        onClick = { onCategoryActivate(label) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            HorizontalDivider()
        }
        Spacer(Modifier.height(2.dp))

        // Bounded viewport: never weight+scroll on the same node ([ContentScrollLayout]).
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            when {
                !selectedHtml.isNullOrBlank() -> {
                    RecipeReadonlyDocument(
                        html = selectedHtml,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    // HTML still loading from disk — avoid a black empty WebView.
                    Box(
                        Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Loading recipe…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Recipe title from `<title>` / `<h1>` for chrome labels (never the bare filename when possible). */
internal fun titleFromRecipeHtml(html: String?): String {
    if (html.isNullOrBlank()) return ""
    Regex("(?is)<title>(.*?)</title>").find(html)?.groupValues?.get(1)?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { return stripSimpleHtml(it) }
    Regex("(?is)<h1[^>]*>(.*?)</h1>").find(html)?.groupValues?.get(1)?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { return stripSimpleHtml(it) }
    return ""
}

/**
 * Platform WebView host for a local `file://` recipe URL and/or inline HTML.
 * Desktop: compose-webview-multiplatform when KCEF is initialized.
 * Android / iOS: system WebView / WKWebView.
 *
 * When [htmlContent] is non-null and non-blank, platforms load that HTML
 * (with [fileUrl] as base when it is a file:// URL). Otherwise [fileUrl] is loaded.
 */
@Composable
expect fun RecipeHtmlWebView(
    fileUrl: String,
    modifier: Modifier = Modifier,
    htmlContent: String? = null,
)
