package recipejar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import recipejar.sample.SampleRecipeJar

/**
 * Mobile prototype root: loads the bundled sample jar via [SampleRecipeJar] /
 * [recipejar.html.RecipeSerializer] and hosts the shared [App] shell with the full
 * Material menu set (Recipe / Edit / Find / Macros / Tools / Help).
 *
 * WebView is off for the first prototype pass; the reader shows parsed HTML text
 * (same fallback path as desktop when KCEF is not ready).
 *
 * @param onExit platform leave path — Android finishes the activity; iOS may show a
 *   status message when programmatic quit is not appropriate.
 */
@Composable
fun MobilePrototypeApp(
    onExit: (() -> Unit)? = null,
) {
    val loaded = remember { SampleRecipeJar.loadRecipes() }
    val items = remember(loaded) {
        loaded.map { (filename, recipe) ->
            RecipeListItem(filename = filename, title = recipe.title)
        }
    }
    val knownLabels = remember(loaded) {
        loaded.flatMap { it.second.labels }.distinct().sorted()
    }

    var shell by remember { mutableStateOf(MobileShellState()) }

    fun requestExit() {
        if (onExit != null) {
            onExit()
        } else {
            // iOS (and hosts without a finish path): discoverable Exit still runs;
            // OS guidelines discourage force-quit, so surface a short status instead.
            shell = shell.copy(
                statusMessage = "Use the Home gesture or app switcher to leave RecipeJar.",
            )
        }
    }

    fun status(msg: String) {
        shell = MobileShellLogic.withStatus(shell, msg)
    }

    val menuActions = MobileMenuActions(
        onNew = { shell = MobileShellLogic.newRecipe(shell) },
        onToggleEdit = { shell = MobileShellLogic.toggleEdit(shell) },
        onSave = { shell = MobileShellLogic.saveRecipe(shell) },
        onRename = { shell = MobileShellLogic.renameRecipe(shell) },
        onImport = { status("Import not available on sample jar (Phase 1A storage)") },
        onExport = {
            status(
                if (shell.selectedFilename != null) {
                    "Export stub: ${shell.selectedFilename}"
                } else {
                    "Select a recipe to export"
                },
            )
        },
        onRemove = { shell = MobileShellLogic.removeRecipe(shell) },
        onExit = { requestExit() },
        onCut = { status("Cut (stub)") },
        onCopy = { status("Copy (stub)") },
        onPaste = { status("Paste (stub)") },
        onSelectAll = { status("Select All (stub)") },
        onFind = { status("Find… (open a full repository for search UI)") },
        onFindAll = { status("Find All (search UI not on sample jar)") },
        onFindTitles = { status("Find Titles (search UI not on sample jar)") },
        onFindLabels = { status("Find Labels (search UI not on sample jar)") },
        onFindNotes = { status("Find Notes (search UI not on sample jar)") },
        onFindIngredients = { status("Find Ingredients (search UI not on sample jar)") },
        onFindProcedures = { status("Find Procedures (search UI not on sample jar)") },
        onManageMacros = { status("Manage Macros… (open a full repository)") },
        onMacro = { name -> status("Macro “$name” applies in edit mode on a full repository") },
        onPreferences = { status("Preferences… (sample jar uses built-in defaults)") },
        onPhoneLayout = { shell = MobileShellLogic.togglePhoneLayout(shell) },
        onAbout = {
            status("RecipeJar — local offline recipe organizer (mobile prototype 1.0.0)")
        },
        macroNames = emptyList(),
    )

    val menuModel = MobileShellLogic.buildMenuModel(
        state = shell,
        actions = menuActions,
    )

    App(
        selectedDir = "sample://family-jar",
        recipes = items,
        selectedFilename = shell.selectedFilename,
        selectedFileUrl = null,
        selectedHtml = shell.selectedHtml,
        editingRecipe = shell.editingRecipe,
        knownLabels = knownLabels,
        welcomeHtml = WELCOME_PROTOTYPE_HTML,
        welcomeFileUrl = null,
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
        onOpenRepo = {
            status("Sample jar is always open; SAF/iCloud open is Phase 1A")
        },
        onSelectRecipe = { filename ->
            shell = MobileShellLogic.selectRecipe(
                state = shell,
                filename = filename,
                html = SampleRecipeJar.htmlFor(filename),
            )
        },
        onRecipeChange = { recipe ->
            shell = MobileShellLogic.applyRecipeChange(shell, recipe)
        },
        onClearSelection = {
            shell = MobileShellLogic.clearSelection(shell)
        },
    )
}

private val WELCOME_PROTOTYPE_HTML =
    """
    <html><body>
    <h1>RecipeJar</h1>
    <p>Mobile prototype — sample family recipes loaded from the real HTML serializer.</p>
    <p>Use the <b>Recipe</b> menu for Toggle Edit, Save, Exit, and other actions.</p>
    <p>Pick a letter and open a recipe to read ingredients and procedure.</p>
    </body></html>
    """.trimIndent()
