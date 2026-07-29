package recipejar

import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer

/**
 * Immutable session state for the mobile prototype shell: selection, read/edit mode,
 * and status. Pure data — no Compose dependencies so unit tests can drive it.
 */
data class MobileShellState(
    val selectedFilename: String? = null,
    val selectedHtml: String? = null,
    val isEditing: Boolean = false,
    val editingRecipe: Recipe? = null,
    val statusMessage: String? = null,
    val forceCompactLayout: Boolean = true,
)

/**
 * Callbacks + presentation inputs for [MobileShellLogic.buildMenuModel].
 * Hosts supply real platform paths (exit) or status/stub implementations.
 */
data class MobileMenuActions(
    val onNew: () -> Unit = {},
    val onToggleEdit: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onRename: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onRemove: () -> Unit = {},
    val onExit: () -> Unit = {},
    val onCut: () -> Unit = {},
    val onCopy: () -> Unit = {},
    val onPaste: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onFind: () -> Unit = {},
    val onFindAll: () -> Unit = {},
    val onFindTitles: () -> Unit = {},
    val onFindLabels: () -> Unit = {},
    val onFindNotes: () -> Unit = {},
    val onFindIngredients: () -> Unit = {},
    val onFindProcedures: () -> Unit = {},
    val onManageMacros: () -> Unit = {},
    val onMacro: (name: String) -> Unit = {},
    val onPreferences: () -> Unit = {},
    val onPhoneLayout: () -> Unit = {},
    val onAbout: () -> Unit = {},
    /** Macro names listed under Macros (empty → placeholder item). */
    val macroNames: List<String> = emptyList(),
)

/**
 * Menu titles matching the desktop Material menu bar (see desktop Main.kt buildMaterialMenus).
 * Shared by the shipped builder and tests so labels cannot drift silently.
 */
object MobileMenuTitles {
    const val RECIPE = "Recipe"
    const val EDIT = "Edit"
    const val FIND = "Find"
    const val MACROS = "Macros"
    const val TOOLS = "Tools"
    const val HELP = "Help"

    const val NEW = "New"
    const val TOGGLE_EDIT = "Toggle Edit"
    const val SAVE = "Save"
    const val RENAME = "Rename"
    const val IMPORT = "Import"
    const val EXPORT = "Export"
    const val REMOVE = "Remove"
    const val EXIT = "Exit"

    const val CUT = "Cut"
    const val COPY = "Copy"
    const val PASTE = "Paste"
    const val SELECT_ALL = "Select All"
    const val FIND_ELLIPSIS = "Find…"

    const val FIND_ALL = "Find All"
    const val FIND_TITLES = "Find Titles"
    const val FIND_LABELS = "Find Labels"
    const val FIND_NOTES = "Find Notes"
    const val FIND_INGREDIENTS = "Find Ingredients"
    const val FIND_PROCEDURES = "Find Procedures"

    const val NO_MACROS_PLACEHOLDER = "(no macros — open a repository)"
    const val MANAGE_MACROS = "Manage Macros…"

    const val PREFERENCES = "Preferences…"
    const val PHONE_LAYOUT = "Phone layout"
    const val PHONE_LAYOUT_ON = "Phone layout ✓"
    const val ABOUT = "About RecipeJar"

    /** Top-level menus in desktop Material order. */
    val TOP_LEVEL: List<String> = listOf(RECIPE, EDIT, FIND, MACROS, TOOLS, HELP)

    /** Required Recipe item titles (order not required for presence checks). */
    val RECIPE_ITEMS: List<String> = listOf(
        NEW, TOGGLE_EDIT, SAVE, RENAME, IMPORT, EXPORT, REMOVE, EXIT,
    )

    val EDIT_ITEMS: List<String> = listOf(CUT, COPY, PASTE, SELECT_ALL, FIND_ELLIPSIS)

    val FIND_ITEMS: List<String> = listOf(
        FIND_ALL, FIND_TITLES, FIND_LABELS, FIND_NOTES, FIND_INGREDIENTS, FIND_PROCEDURES,
    )

    val TOOLS_ITEMS_BASE: List<String> = listOf(PREFERENCES)
    val HELP_ITEMS: List<String> = listOf(ABOUT)

    // Back-compat aliases used by earlier tests
    const val RECIPE_MENU = RECIPE
}

/**
 * Pure mobile shell transitions and full Material menu wiring used by [MobilePrototypeApp].
 *
 * Edit mode binds a real [Recipe] parsed from the selected HTML via [RecipeSerializer]
 * (or a caller-supplied [parseRecipe]). Leaving edit returns to read mode without
 * clearing the selection or HTML content.
 */
object MobileShellLogic {

    fun selectRecipe(
        state: MobileShellState,
        filename: String,
        html: String?,
        parseRecipe: (String) -> Recipe = RecipeSerializer::parse,
    ): MobileShellState {
        val next = state.copy(
            selectedFilename = filename,
            selectedHtml = html,
            statusMessage = null,
        )
        return if (next.isEditing) {
            enterEdit(next, parseRecipe)
        } else {
            next.copy(editingRecipe = null)
        }
    }

    fun clearSelection(state: MobileShellState): MobileShellState =
        state.copy(
            selectedFilename = null,
            selectedHtml = null,
            isEditing = false,
            editingRecipe = null,
        )

    /**
     * Toggle read ↔ edit. Entering edit requires a selected recipe with HTML and
     * binds [MobileShellState.editingRecipe] from that HTML. Leaving edit clears
     * the editor model but keeps selection + HTML for the reader.
     */
    fun toggleEdit(
        state: MobileShellState,
        parseRecipe: (String) -> Recipe = RecipeSerializer::parse,
    ): MobileShellState =
        if (state.isEditing) leaveEdit(state) else enterEdit(state, parseRecipe)

    fun enterEdit(
        state: MobileShellState,
        parseRecipe: (String) -> Recipe = RecipeSerializer::parse,
    ): MobileShellState {
        val html = state.selectedHtml
        if (state.selectedFilename == null || html.isNullOrBlank()) {
            return state.copy(
                statusMessage = "Select a recipe before editing",
            )
        }
        val recipe = parseRecipe(html)
        return state.copy(
            isEditing = true,
            editingRecipe = recipe,
            statusMessage = null,
        )
    }

    fun leaveEdit(state: MobileShellState): MobileShellState =
        state.copy(
            isEditing = false,
            editingRecipe = null,
            statusMessage = null,
        )

    /** Push structured form edits; keep [selectedHtml] in sync for the reader buffer. */
    fun applyRecipeChange(
        state: MobileShellState,
        recipe: Recipe,
        serialize: (Recipe) -> String = { RecipeSerializer.serialize(it, "browser-footer") },
    ): MobileShellState =
        state.copy(
            editingRecipe = recipe,
            selectedHtml = serialize(recipe),
        )

    fun withStatus(state: MobileShellState, message: String): MobileShellState =
        state.copy(statusMessage = message)

    fun togglePhoneLayout(state: MobileShellState): MobileShellState {
        val next = !state.forceCompactLayout
        return state.copy(
            forceCompactLayout = next,
            statusMessage = if (next) "Phone layout on" else "Phone layout off",
        )
    }

    /**
     * In-memory "New" for the sample-jar host (no durable FS).
     * Creates an Untitled recipe and enters edit mode.
     */
    fun newRecipe(state: MobileShellState): MobileShellState {
        val recipe = Recipe(title = "Untitled")
        val html = RecipeSerializer.serialize(recipe, "browser-footer")
        return state.copy(
            selectedFilename = "Untitled.html",
            selectedHtml = html,
            isEditing = true,
            editingRecipe = recipe,
            statusMessage = "New recipe (in memory — sample jar is read-only)",
        )
    }

    fun saveRecipe(state: MobileShellState): MobileShellState {
        if (state.selectedFilename == null) {
            return state.copy(statusMessage = "Nothing to save")
        }
        return state.copy(
            statusMessage = "Save is not durable on this prototype (sample jar is read-only)",
        )
    }

    fun renameRecipe(state: MobileShellState): MobileShellState {
        val cur = state.selectedFilename
            ?: return state.copy(statusMessage = "Select a recipe to rename")
        val nextName = cur.removeSuffix(".html") + "-renamed.html"
        return state.copy(
            selectedFilename = nextName,
            statusMessage = "Renamed to $nextName (in memory)",
        )
    }

    fun removeRecipe(state: MobileShellState): MobileShellState {
        if (state.selectedFilename == null) {
            return state.copy(statusMessage = "Nothing to remove")
        }
        return clearSelection(state).copy(
            statusMessage = "Removed from session (sample jar unchanged)",
        )
    }

    /**
     * Full Material menu model matching desktop structure:
     * Recipe / Edit / Find / Macros / Tools / Help.
     */
    fun buildMenuModel(
        state: MobileShellState,
        actions: MobileMenuActions,
    ): AppMenuModel {
        val hasSelection = state.selectedFilename != null
        val hasRepo = true // sample jar is always "open"
        val canToggle = hasSelection || state.isEditing
        val editEnabled = hasSelection

        fun item(
            title: String,
            enabled: Boolean = true,
            onClick: () -> Unit,
        ) = AppMenuEntry.Item(title = title, enabled = enabled, onClick = onClick)

        val macroEntries: List<AppMenuEntry> = buildList {
            if (actions.macroNames.isEmpty()) {
                add(
                    item(
                        title = MobileMenuTitles.NO_MACROS_PLACEHOLDER,
                        enabled = false,
                        onClick = {},
                    ),
                )
            } else {
                actions.macroNames.forEach { name ->
                    add(
                        item(
                            title = name,
                            enabled = state.isEditing && state.selectedHtml != null,
                            onClick = { actions.onMacro(name) },
                        ),
                    )
                }
            }
            add(AppMenuEntry.Separator)
            add(
                item(
                    title = MobileMenuTitles.MANAGE_MACROS,
                    enabled = hasRepo,
                    onClick = actions.onManageMacros,
                ),
            )
        }

        val phoneTitle = if (state.forceCompactLayout) {
            MobileMenuTitles.PHONE_LAYOUT_ON
        } else {
            MobileMenuTitles.PHONE_LAYOUT
        }

        return AppMenuModel(
            menus = listOf(
                AppMenu(
                    title = MobileMenuTitles.RECIPE,
                    entries = listOf(
                        item(MobileMenuTitles.NEW, enabled = hasRepo, onClick = actions.onNew),
                        item(MobileMenuTitles.TOGGLE_EDIT, enabled = canToggle, onClick = actions.onToggleEdit),
                        item(MobileMenuTitles.SAVE, enabled = hasSelection, onClick = actions.onSave),
                        item(MobileMenuTitles.RENAME, enabled = hasSelection, onClick = actions.onRename),
                        AppMenuEntry.Separator,
                        item(MobileMenuTitles.IMPORT, enabled = hasRepo, onClick = actions.onImport),
                        item(MobileMenuTitles.EXPORT, enabled = hasSelection, onClick = actions.onExport),
                        AppMenuEntry.Separator,
                        item(MobileMenuTitles.REMOVE, enabled = hasSelection, onClick = actions.onRemove),
                        AppMenuEntry.Separator,
                        item(MobileMenuTitles.EXIT, enabled = true, onClick = actions.onExit),
                    ),
                ),
                AppMenu(
                    title = MobileMenuTitles.EDIT,
                    entries = listOf(
                        item(MobileMenuTitles.CUT, enabled = editEnabled, onClick = actions.onCut),
                        item(MobileMenuTitles.COPY, enabled = editEnabled, onClick = actions.onCopy),
                        item(MobileMenuTitles.PASTE, enabled = editEnabled, onClick = actions.onPaste),
                        item(MobileMenuTitles.SELECT_ALL, enabled = editEnabled, onClick = actions.onSelectAll),
                        AppMenuEntry.Separator,
                        item(MobileMenuTitles.FIND_ELLIPSIS, enabled = hasRepo, onClick = actions.onFind),
                    ),
                ),
                AppMenu(
                    title = MobileMenuTitles.FIND,
                    entries = listOf(
                        item(MobileMenuTitles.FIND_ALL, enabled = hasRepo, onClick = actions.onFindAll),
                        item(MobileMenuTitles.FIND_TITLES, enabled = hasRepo, onClick = actions.onFindTitles),
                        item(MobileMenuTitles.FIND_LABELS, enabled = hasRepo, onClick = actions.onFindLabels),
                        item(MobileMenuTitles.FIND_NOTES, enabled = hasRepo, onClick = actions.onFindNotes),
                        item(MobileMenuTitles.FIND_INGREDIENTS, enabled = hasRepo, onClick = actions.onFindIngredients),
                        item(MobileMenuTitles.FIND_PROCEDURES, enabled = hasRepo, onClick = actions.onFindProcedures),
                    ),
                ),
                AppMenu(title = MobileMenuTitles.MACROS, entries = macroEntries),
                AppMenu(
                    title = MobileMenuTitles.TOOLS,
                    entries = listOf(
                        item(MobileMenuTitles.PREFERENCES, onClick = actions.onPreferences),
                        item(phoneTitle, onClick = actions.onPhoneLayout),
                    ),
                ),
                AppMenu(
                    title = MobileMenuTitles.HELP,
                    entries = listOf(
                        item(MobileMenuTitles.ABOUT, onClick = actions.onAbout),
                    ),
                ),
            ),
        )
    }

    /**
     * Convenience overload used by older call sites / focused tests that only need
     * Toggle Edit + Exit wiring (full model still returned).
     */
    fun buildMenuModel(
        state: MobileShellState,
        onToggleEdit: () -> Unit,
        onExit: () -> Unit,
    ): AppMenuModel = buildMenuModel(
        state = state,
        actions = MobileMenuActions(
            onToggleEdit = onToggleEdit,
            onExit = onExit,
        ),
    )

    /** Titles of enabled item entries across all menus (for tests / diagnostics). */
    fun enabledItemTitles(model: AppMenuModel): List<String> =
        model.menus.flatMap { menu ->
            menu.entries.mapNotNull { entry ->
                (entry as? AppMenuEntry.Item)?.takeIf { it.enabled }?.title
            }
        }

    /** All item titles (enabled or not). */
    fun allItemTitles(model: AppMenuModel): List<String> =
        model.menus.flatMap { menu ->
            menu.entries.mapNotNull { (it as? AppMenuEntry.Item)?.title }
        }

    fun menuTitles(model: AppMenuModel): List<String> = model.menus.map { it.title }

    fun itemTitlesInMenu(model: AppMenuModel, menuTitle: String): List<String> =
        model.menus.firstOrNull { it.title == menuTitle }
            ?.entries
            ?.mapNotNull { (it as? AppMenuEntry.Item)?.title }
            .orEmpty()

    fun findItem(model: AppMenuModel, title: String): AppMenuEntry.Item? =
        model.menus.flatMap { it.entries }
            .filterIsInstance<AppMenuEntry.Item>()
            .firstOrNull { it.title == title }
}
