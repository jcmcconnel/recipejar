package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import recipejar.macro.MacroIo
import recipejar.persistence.OkioRecipeRepository
import recipejar.persistence.seedIfEmpty
import recipejar.recipe.UnitDef
import recipejar.recipe.UnitsCatalog
import recipejar.sample.SampleRecipeJar
import recipejar.search.SearchScope

/**
 * Mobile / iOS host: fixed library under [recipeLibraryRootPath], Material menus
 * without Open repository / Exit, import/export via platform share APIs.
 */
@Composable
fun MobilePrototypeApp(
    onExit: (() -> Unit)? = null,
) {
    val libraryPath = remember {
        try {
            recipeLibraryRootPath()
        } catch (_: Exception) {
            // Desktop tests / hosts without install — fall back to empty temp-like path name
            ""
        }
    }
    val repo = remember(libraryPath) {
        if (libraryPath.isBlank()) {
            null
        } else {
            OkioRecipeRepository(libraryPath).also { r ->
                r.seedIfEmpty(SampleRecipeJar.entries.map { it.filename to it.html })
            }
        }
    }

    fun loadItems(): List<RecipeListItem> {
        val r = repo ?: return SampleRecipeJar.loadRecipes().map { (fn, recipe) ->
            RecipeListItem(fn, recipe.title, recipe.labels.toList())
        }
        return r.listRecipes().map { fn ->
            try {
                val recipe = r.loadRecipe(fn)
                RecipeListItem(fn, recipe.title, recipe.labels.toList())
            } catch (_: Exception) {
                RecipeListItem(fn, fn.removeSuffix(".html"), emptyList())
            }
        }
    }

    var items by remember { mutableStateOf(loadItems()) }
    var knownLabels by remember {
        mutableStateOf(
            items.flatMap { it.labels }.distinct().sorted(),
        )
    }
    var shell by remember { mutableStateOf(MobileShellState()) }
    var unitDefs by remember {
        mutableStateOf(
            UnitsCatalog.parse("Cups,Cup\nTbsps,Tbsp\ntsps,tsp\noz,oz\n"),
        )
    }
    var unitCatalog by remember {
        mutableStateOf(UnitsCatalog.dropdownLabels(unitDefs).filter { it.isNotEmpty() })
    }
    var macros by remember { mutableStateOf(MacroIo.DEFAULT_MACROS) }
    var contentModalKind by remember { mutableStateOf<MobileContentModal?>(null) }
    var appearanceId by remember { mutableStateOf(AppearanceTheme.DEFAULT_ID) }
    var appearanceDark by remember { mutableStateOf(false) }
    var searchScopes by remember {
        mutableStateOf(setOf(SearchScope.TITLES, SearchScope.LABELS))
    }

    fun refreshCatalog() {
        items = loadItems()
        knownLabels = items.flatMap { it.labels }.distinct().sorted()
    }

    fun status(msg: String) {
        shell = MobileShellLogic.withStatus(shell, msg)
    }

    fun openSearch(scopes: Set<SearchScope>) {
        searchScopes = scopes
        contentModalKind = MobileContentModal.Search
    }

    fun htmlFor(filename: String): String? {
        val r = repo
        return if (r != null) {
            try {
                r.loadRecipeHtml(filename)
            } catch (_: Exception) {
                SampleRecipeJar.htmlFor(filename)
            }
        } else {
            SampleRecipeJar.htmlFor(filename)
        }
    }

    val menuActions = MobileMenuActions(
        onOpenRepo = { status("This device uses a fixed recipe library") },
        onNew = {
            val recipe = Recipe(title = "Untitled")
            val html = RecipeSerializer.serialize(recipe, "browser-footer")
            shell = shell.copy(
                selectedFilename = "Untitled.html",
                selectedHtml = html,
                isEditing = true,
                editingRecipe = recipe,
                statusMessage = "New recipe (unsaved)",
            )
        },
        onToggleEdit = { shell = MobileShellLogic.toggleEdit(shell) },
        onSave = {
            val r = repo
            val editing = shell.editingRecipe
            if (r == null) {
                shell = MobileShellLogic.saveRecipe(shell)
            } else if (editing == null || shell.selectedFilename == null) {
                status("Nothing to save")
            } else {
                try {
                    val original = shell.selectedFilename!!.takeIf {
                        !shell.selectedFilename!!.startsWith("Untitled")
                    }
                    r.saveRecipe(editing, originalFilename = original)
                    val name = r.filenameFor(editing)
                    val disk = r.loadRecipeHtml(name)
                    shell = shell.copy(
                        selectedFilename = name,
                        selectedHtml = disk,
                        isEditing = false,
                        editingRecipe = null,
                        statusMessage = "Saved $name",
                    )
                    refreshCatalog()
                } catch (e: Exception) {
                    status("Save failed: ${e.message}")
                }
            }
        },
        onRename = { shell = MobileShellLogic.renameRecipe(shell) },
        onImport = {
            platformImportHtml { name, html, error ->
                when {
                    error != null -> status(error)
                    html == null -> { /* cancelled */ }
                    repo == null -> status("No library available for import")
                    else -> {
                        try {
                            val used = repo.importHtmlBytes(html, name)
                            refreshCatalog()
                            shell = MobileShellLogic.selectRecipe(
                                shell,
                                used,
                                repo.loadRecipeHtml(used),
                            )
                            status("Imported $used")
                        } catch (e: Exception) {
                            status("Import failed: ${e.message}")
                        }
                    }
                }
            }
        },
        onExport = {
            val fn = shell.selectedFilename
            val r = repo
            if (fn == null) {
                status("Select a recipe to export")
            } else if (r == null) {
                status("Export needs a library")
            } else {
                try {
                    val html = r.exportRecipeHtml(fn)
                    platformShareText(fn.removeSuffix(".html") + "-export.html", html) {
                        status("Shared export of $fn")
                    }
                } catch (e: Exception) {
                    status("Export failed: ${e.message}")
                }
            }
        },
        onExportZip = {
            status("Whole-library zip share is coming; export a single recipe for now")
        },
        onRemove = {
            val fn = shell.selectedFilename
            val r = repo
            if (fn == null) {
                status("Nothing to remove")
            } else if (r == null) {
                shell = MobileShellLogic.removeRecipe(shell)
            } else {
                try {
                    r.deleteRecipe(fn)
                    shell = MobileShellLogic.clearSelection(shell).copy(
                        statusMessage = "Removed $fn",
                    )
                    refreshCatalog()
                } catch (e: Exception) {
                    status("Remove failed: ${e.message}")
                }
            }
        },
        onExit = {
            if (onExit != null) onExit() else status("Use the Home gesture to leave RecipeJar")
        },
        onCut = { status("Cut uses the system clipboard (not available on this prototype)") },
        onCopy = { status("Copy uses the system clipboard (not available on this prototype)") },
        onPaste = { status("Paste uses the system clipboard (not available on this prototype)") },
        onSelectAll = { status("Select All uses system text selection") },
        onFind = { openSearch(setOf(SearchScope.TITLES, SearchScope.LABELS)) },
        onFindAll = {
            openSearch(
                setOf(
                    SearchScope.TITLES, SearchScope.LABELS, SearchScope.NOTES,
                    SearchScope.INGREDIENTS, SearchScope.PROCEDURE,
                ),
            )
        },
        onFindTitles = { openSearch(setOf(SearchScope.TITLES)) },
        onFindLabels = { openSearch(setOf(SearchScope.LABELS)) },
        onFindNotes = { openSearch(setOf(SearchScope.NOTES)) },
        onFindIngredients = { openSearch(setOf(SearchScope.INGREDIENTS)) },
        onFindProcedures = { openSearch(setOf(SearchScope.PROCEDURE)) },
        onManageMacros = { contentModalKind = MobileContentModal.Macros },
        onMacro = { name ->
            val macro = macros.firstOrNull { it.name == name }
            if (macro == null) {
                status("Macro “$name” not found")
            } else if (!shell.isEditing || shell.editingRecipe == null) {
                status("Macros apply in edit mode (Recipe → Toggle Edit)")
            } else {
                val recipe = shell.editingRecipe!!
                val next = recipe.deepCopy().also { it.notes = it.notes + macro.text }
                shell = MobileShellLogic.applyRecipeChange(shell, next)
                status("Applied macro “$name” to notes")
            }
        },
        onPreferences = { contentModalKind = MobileContentModal.Preferences },
        onUnits = { contentModalKind = MobileContentModal.Units },
        onConverter = { contentModalKind = MobileContentModal.Converter },
        onPhoneLayout = { shell = MobileShellLogic.togglePhoneLayout(shell) },
        onHelpWeb = {
            val opened = openExternalUrl(HelpLinks.WEB_URL)
            status(if (opened) "Opened ${HelpLinks.WEB_URL}" else HelpLinks.WEB_URL)
        },
        onAbout = {
            val where = if (libraryPath.isBlank()) "sample" else libraryPath
            status("RecipeJar — library: $where")
        },
        macroNames = macros.map { it.name },
    )

    val menuModel = MobileShellLogic.buildMenuModel(
        state = shell,
        actions = menuActions,
        includeDesktopRepoChrome = false,
    )

    val contentModal: (@Composable () -> Unit)? = when (contentModalKind) {
        MobileContentModal.Preferences -> {
            {
                PreferencesDialog(
                    initialRepoPath = libraryPath.ifBlank { "app://library" },
                    initialAuthorName = "",
                    initialWelcomeFilePath = "",
                    initialAppearanceId = appearanceId,
                    initialAppearanceDark = appearanceDark,
                    onBrowseRepo = { null },
                    onBrowseWelcome = null,
                    onSave = { _, _, _, schemeId, dark ->
                        appearanceId = AppearanceTheme.parse(schemeId).id
                        appearanceDark = dark
                        status("Preferences saved for this session")
                        contentModalKind = null
                        null
                    },
                    onDismiss = { contentModalKind = null },
                    useDialog = false,
                )
            }
        }
        MobileContentModal.Units -> {
            {
                UnitsManagerDialog(
                    initial = unitDefs,
                    onSave = { list: List<UnitDef> ->
                        unitDefs = list
                        unitCatalog = UnitsCatalog.dropdownLabels(list).filter { it.isNotEmpty() }
                        status("Updated ${list.size} unit(s) for this session")
                        contentModalKind = null
                    },
                    onDismiss = { contentModalKind = null },
                    useDialog = false,
                )
            }
        }
        MobileContentModal.Search -> {
            {
                SearchDialog(
                    recipes = items,
                    initialScopes = searchScopes,
                    fieldTextProvider = {
                        withContext(Dispatchers.Default) {
                            items.associate { item ->
                                val r = try {
                                    repo?.loadRecipe(item.filename)
                                } catch (_: Exception) {
                                    null
                                }
                                item.filename to mapOf(
                                    SearchScope.LABELS to (r?.getLabelsAsString() ?: item.labels.joinToString()),
                                    SearchScope.NOTES to (r?.notes ?: ""),
                                    SearchScope.INGREDIENTS to (
                                        r?.ingredients?.joinToString(" ") {
                                            "${it.quantity} ${it.unit} ${it.name}"
                                        } ?: ""
                                    ),
                                    SearchScope.PROCEDURE to (r?.procedure ?: ""),
                                )
                            }
                        }
                    },
                    onSelect = { filename ->
                        shell = MobileShellLogic.selectRecipe(shell, filename, htmlFor(filename))
                        contentModalKind = null
                    },
                    onDismiss = { contentModalKind = null },
                    useDialog = false,
                )
            }
        }
        MobileContentModal.Converter -> {
            {
                UnitConverterDialog(
                    units = unitDefs,
                    onDismiss = { contentModalKind = null },
                    useDialog = false,
                )
            }
        }
        MobileContentModal.Macros -> {
            {
                MacroManagerDialog(
                    initial = macros,
                    onSave = { list ->
                        macros = list
                        status("Saved ${list.size} macro(s) for this session")
                        contentModalKind = null
                    },
                    onDismiss = { contentModalKind = null },
                    useDialog = false,
                )
            }
        }
        null -> null
    }

    App(
        selectedDir = libraryPath.ifBlank { "sample://family-jar" },
        recipes = items,
        selectedFilename = shell.selectedFilename,
        selectedFileUrl = null,
        selectedHtml = shell.selectedHtml,
        editingRecipe = shell.editingRecipe,
        knownLabels = knownLabels,
        unitCatalog = unitCatalog,
        welcomeHtml = WELCOME_HTML,
        welcomeFileUrl = null,
        welcomeWebViewEnabled = contentModalKind == null,
        webViewReady = false,
        isEditing = shell.isEditing,
        statusMessage = shell.statusMessage,
        materialMenus = menuModel,
        forceCompactLayout = shell.forceCompactLayout,
        onForceCompactChange = {
            shell = shell.copy(
                forceCompactLayout = it,
                statusMessage = if (it) "Phone layout on" else "Phone layout off",
            )
        },
        onOpenRepo = { status("This device uses a fixed recipe library") },
        onSelectRecipe = { filename ->
            shell = MobileShellLogic.selectRecipe(shell, filename, htmlFor(filename))
        },
        onRecipeChange = { recipe ->
            shell = MobileShellLogic.applyRecipeChange(shell, recipe)
        },
        onClearSelection = {
            shell = MobileShellLogic.clearSelection(shell)
        },
        contentModal = contentModal,
        appearanceId = appearanceId,
        appearanceDark = appearanceDark,
    )
}

private enum class MobileContentModal {
    Preferences,
    Units,
    Converter,
    Search,
    Macros,
}

private val WELCOME_HTML =
    """
    <html><body>
    <h1>RecipeJar</h1>
    <p>Your recipes live in the app library (Application Support on iOS).</p>
    <p>Use <b>Import</b> / <b>Export</b> with the system share sheet. Search via Edit → Find…</p>
    </body></html>
    """.trimIndent()
