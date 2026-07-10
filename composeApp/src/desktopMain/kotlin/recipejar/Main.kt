package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import recipejar.actions.*
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import recipejar.persistence.FileSystemRecipeRepository
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

/**
 * Desktop entry: open a recipe repository directory, list via FileSystemRecipeRepository,
 * drive alpha-tab index + reader, and MenuBar from ActionRegistry (PR-5 merged for PR-6).
 *
 * KCEF bootstrap enables compose-webview-multiplatform for file:// recipe HTML.
 * If init fails or is still in progress, App falls back to scrollable HTML text.
 */
fun main() = application {
    val selectedDir = remember { mutableStateOf<String?>(null) }
    val recipes = remember { mutableStateOf<List<RecipeListItem>>(emptyList()) }
    val selectedFilename = remember { mutableStateOf<String?>(null) }
    val selectedHtml = remember { mutableStateOf<String?>(null) }
    val selectedFileUrl = remember { mutableStateOf<String?>(null) }
    val webViewReady = remember { mutableStateOf(false) }
    val restartRequired = remember { mutableStateOf(false) }
    val indexLoading = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isRecipeOpen: () -> Boolean = { selectedFilename.value != null }

    fun setWebViewReadyOnMain(ready: Boolean) {
        scope.launch(Dispatchers.Main.immediate) {
            webViewReady.value = ready
        }
    }

    fun setRestartRequiredOnMain(required: Boolean) {
        scope.launch(Dispatchers.Main.immediate) {
            restartRequired.value = required
            if (required) webViewReady.value = false
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading { }
                        onInitialized {
                            setWebViewReadyOnMain(true)
                        }
                    }
                    settings {
                        cachePath = File("kcef-cache").absolutePath
                    }
                }, onError = {
                    setWebViewReadyOnMain(false)
                }, onRestartRequired = {
                    setRestartRequiredOnMain(true)
                })
            } catch (_: Throwable) {
                withContext(Dispatchers.Main) {
                    webViewReady.value = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                KCEF.disposeBlocking()
            } catch (_: Throwable) {
            }
        }
    }

    fun pickDirectory() {
        val chooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Open recipe repository"
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val dir = chooser.selectedFile
            if (dir != null && dir.isDirectory) {
                val path = dir.absolutePath
                selectedDir.value = path
                selectedFilename.value = null
                selectedHtml.value = null
                selectedFileUrl.value = null
                isEditing.value = false
                recipes.value = emptyList()
                indexLoading.value = true
                scope.launch {
                    try {
                        val loaded = withContext(Dispatchers.IO) {
                            val repo = FileSystemRecipeRepository(path)
                            loadRecipeIndex(repo)
                        }
                        if (selectedDir.value == path) {
                            recipes.value = loaded
                        }
                    } catch (_: Exception) {
                        if (selectedDir.value == path) {
                            recipes.value = emptyList()
                        }
                    } finally {
                        if (selectedDir.value == path) {
                            indexLoading.value = false
                        }
                    }
                }
            }
        }
    }

    fun selectRecipe(filename: String) {
        val dir = selectedDir.value ?: return
        selectedFilename.value = filename
        isEditing.value = false
        val file = File(dir, filename)
        if (!file.isFile) {
            selectedFileUrl.value = null
            selectedHtml.value = null
            return
        }
        selectedFileUrl.value = file.toURI().toString()
        selectedHtml.value = null
        scope.launch {
            val html = withContext(Dispatchers.IO) {
                try {
                    file.readText(Charsets.UTF_8)
                } catch (_: Exception) {
                    null
                }
            }
            if (selectedFilename.value != filename || selectedDir.value != dir) return@launch
            if (html == null) {
                selectedFileUrl.value = null
                selectedHtml.value = null
            } else {
                selectedHtml.value = html
            }
        }
    }

    fun refreshIndexAndSelect(dir: String, filename: String) {
        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                try {
                    loadRecipeIndex(FileSystemRecipeRepository(dir))
                } catch (_: Exception) {
                    emptyList()
                }
            }
            if (selectedDir.value != dir) return@launch
            recipes.value = loaded
            selectedFilename.value = filename
            val file = File(dir, filename)
            if (file.isFile) {
                selectedFileUrl.value = file.toURI().toString()
                val html = withContext(Dispatchers.IO) {
                    try {
                        file.readText(Charsets.UTF_8)
                    } catch (_: Exception) {
                        null
                    }
                }
                if (selectedDir.value == dir && selectedFilename.value == filename) {
                    selectedHtml.value = html
                }
            }
        }
    }

    /**
     * Persist editor buffer via [FileSystemRecipeRepository].
     * Parses HTML (full document preferred), serializes with browser-footer, renames on title change.
     */
    fun saveCurrentRecipe() {
        val dir = selectedDir.value ?: return
        val filename = selectedFilename.value ?: return
        val html = selectedHtml.value ?: return
        if (html.isBlank()) {
            println("Save: empty buffer")
            return
        }
        scope.launch {
            try {
                val savedName = withContext(Dispatchers.IO) {
                    val repo = FileSystemRecipeRepository(dir)
                    val recipe = RecipeSerializer.parse(html)
                    if (recipe.title.isBlank()) {
                        recipe.title = stripHtmlExtension(filename).ifBlank { "Untitled" }
                    }
                    // Title-rename support: pass existing on-disk name when present
                    val original = filename.takeIf { File(dir, it).isFile }
                    repo.saveRecipe(recipe, originalFilename = original)
                    repo.filenameFor(recipe)
                }
                if (selectedDir.value != dir) return@launch
                refreshIndexAndSelect(dir, savedName)
            } catch (e: Exception) {
                println("Save failed: ${e.message}")
            }
        }
    }

    fun newRecipe() {
        val dir = selectedDir.value
        val recipe = Recipe(
            title = "Untitled",
            notes = "A little about this recipe,<br/>\nwhere I got it why I like it, etc.<br/>\nMakes 1 serving.",
            procedure = "",
        )
        val html = RecipeSerializer.serialize(recipe, "browser-footer")
        selectedFilename.value = "Untitled.html"
        selectedHtml.value = html
        selectedFileUrl.value = null
        isEditing.value = true
        // Optional: no disk write until Save; dir may still be null (enabled only when open later)
        if (dir == null) {
            println("New: no repository open — buffer only until Open repository + Save")
        }
    }

    val registry = remember {
        ActionRegistry().also { reg ->
            val isMac = System.getProperty("os.name").lowercase().contains("mac")
            val isOpen = isRecipeOpen

            reg.register(
                ActionIds.FILE_NEW,
                Command(
                    id = ActionIds.FILE_NEW,
                    title = "New",
                    mnemonic = 'N',
                    shortcut = KeyCombo('N', meta = isMac, ctrl = !isMac),
                    execute = { newRecipe() },
                    enabled = { selectedDir.value != null }
                )
            )

            reg.register(
                ActionIds.FILE_TOGGLE_EDIT,
                Command(
                    id = ActionIds.FILE_TOGGLE_EDIT,
                    title = "Toggle Edit",
                    mnemonic = 'O',
                    shortcut = KeyCombo('O', meta = isMac, ctrl = !isMac),
                    execute = { isEditing.value = !isEditing.value },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_SAVE,
                Command(
                    id = ActionIds.FILE_SAVE,
                    title = "Save",
                    mnemonic = 'S',
                    shortcut = KeyCombo('S', meta = isMac, ctrl = !isMac),
                    execute = { saveCurrentRecipe() },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_DELETE,
                Command(
                    id = ActionIds.FILE_DELETE,
                    title = "Remove",
                    mnemonic = 'R',
                    shortcut = KeyCombo(delete = true),
                    execute = {
                        println("Delete stub: ${selectedFilename.value}")
                        selectedFilename.value = null
                        selectedHtml.value = null
                        selectedFileUrl.value = null
                        isEditing.value = false
                    },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_RENAME,
                Command(
                    id = ActionIds.FILE_RENAME,
                    title = "Rename",
                    execute = {
                        selectedFilename.value?.let { cur ->
                            selectedFilename.value = cur.removeSuffix(".html") + "-renamed.html"
                        } ?: println("Rename: no current recipe open")
                    },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_IMPORT,
                Command(
                    id = ActionIds.FILE_IMPORT,
                    title = "Import",
                    execute = { println("Import stub") },
                    enabled = { selectedDir.value != null }
                )
            )

            reg.register(
                ActionIds.FILE_EXPORT,
                Command(
                    id = ActionIds.FILE_EXPORT,
                    title = "Export",
                    execute = { println("Export stub for: ${selectedFilename.value}") },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_PRINT,
                Command(
                    id = ActionIds.FILE_PRINT,
                    title = "Print",
                    mnemonic = 'P',
                    shortcut = KeyCombo('P', meta = isMac, ctrl = !isMac),
                    execute = { println("Print stub") },
                    enabled = { false }
                )
            )

            reg.register(
                ActionIds.FILE_EXIT,
                Command(
                    id = ActionIds.FILE_EXIT,
                    title = "Exit",
                    mnemonic = 'X',
                    execute = { exitApplication() }
                )
            )

            val editEnabled = isOpen
            reg.register(ActionIds.EDIT_CUT, Command(id = ActionIds.EDIT_CUT, title = "Cut", execute = { println("Cut stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_COPY, Command(id = ActionIds.EDIT_COPY, title = "Copy", execute = { println("Copy stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_PASTE, Command(id = ActionIds.EDIT_PASTE, title = "Paste", execute = { println("Paste stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_SELECT_ALL, Command(id = ActionIds.EDIT_SELECT_ALL, title = "Select All", execute = { println("SelectAll stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_MACROS, Command(id = ActionIds.EDIT_MACROS, title = "Macros", execute = { println("Macros stub") }))
            reg.register(ActionIds.EDIT_FIND, Command(id = ActionIds.EDIT_FIND, title = "Find", execute = { println("Find stub") }, enabled = isOpen))
            reg.register(ActionIds.FIND_ALL, Command(id = ActionIds.FIND_ALL, title = "Find All", execute = { println("FindAll stub") }))
            reg.register(ActionIds.FIND_TITLES, Command(id = ActionIds.FIND_TITLES, title = "Find Titles", execute = { println("FindTitles stub") }))
            reg.register(ActionIds.FIND_LABELS, Command(id = ActionIds.FIND_LABELS, title = "Find Labels", execute = { println("FindLabels stub") }))
            reg.register(ActionIds.FIND_NOTES, Command(id = ActionIds.FIND_NOTES, title = "Find Notes", execute = { println("FindNotes stub") }))
            reg.register(ActionIds.FIND_INGREDIENTS, Command(id = ActionIds.FIND_INGREDIENTS, title = "Find Ingredients", execute = { println("FindIngredients stub") }))
            reg.register(ActionIds.FIND_PROCEDURES, Command(id = ActionIds.FIND_PROCEDURES, title = "Find Procedures", execute = { println("FindProcedures stub") }))
        }
    }

    fun toKeyShortcut(combo: KeyCombo?): KeyShortcut? {
        if (combo == null) return null
        val k = if (combo.delete) Key.Delete else when (combo.key.uppercaseChar()) {
            'N' -> Key.N
            'O' -> Key.O
            'S' -> Key.S
            'X' -> Key.X
            'P' -> Key.P
            'C' -> Key.C
            'V' -> Key.V
            'A' -> Key.A
            else -> Key.Unknown
        }
        return KeyShortcut(key = k, ctrl = combo.ctrl, meta = combo.meta, alt = combo.alt, shift = combo.shift)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "RecipeJar",
    ) {
        MenuBar {
            Menu("Recipe", mnemonic = 'R') {
                val c = registry.require(ActionIds.FILE_NEW)
                Item(c.title, onClick = { c.execute(ActionContext()) }, enabled = c.enabled(), shortcut = toKeyShortcut(c.shortcut))
                val t = registry.require(ActionIds.FILE_TOGGLE_EDIT)
                Item(t.title, onClick = { t.execute(ActionContext()) }, enabled = t.enabled(), shortcut = toKeyShortcut(t.shortcut))
                val s = registry.require(ActionIds.FILE_SAVE)
                Item(s.title, onClick = { s.execute(ActionContext()) }, enabled = s.enabled(), shortcut = toKeyShortcut(s.shortcut))
                val rn = registry.require(ActionIds.FILE_RENAME)
                Item(rn.title, onClick = { rn.execute(ActionContext()) }, enabled = rn.enabled())
                Separator()
                val im = registry.require(ActionIds.FILE_IMPORT)
                Item(im.title, onClick = { im.execute(ActionContext()) }, enabled = im.enabled())
                val ex = registry.require(ActionIds.FILE_EXPORT)
                Item(ex.title, onClick = { ex.execute(ActionContext()) }, enabled = ex.enabled())
                Separator()
                val del = registry.require(ActionIds.FILE_DELETE)
                Item(del.title, onClick = { del.execute(ActionContext()) }, enabled = del.enabled())
                Separator()
                val exi = registry.require(ActionIds.FILE_EXIT)
                Item(exi.title, onClick = { exi.execute(ActionContext()) }, shortcut = toKeyShortcut(exi.shortcut))
            }
            Menu("Edit", mnemonic = 'E') {
                val cut = registry.require(ActionIds.EDIT_CUT)
                Item(cut.title, onClick = { cut.execute(ActionContext()) }, enabled = cut.enabled())
                val copy = registry.require(ActionIds.EDIT_COPY)
                Item(copy.title, onClick = { copy.execute(ActionContext()) }, enabled = copy.enabled())
                val paste = registry.require(ActionIds.EDIT_PASTE)
                Item(paste.title, onClick = { paste.execute(ActionContext()) }, enabled = paste.enabled())
                val sel = registry.require(ActionIds.EDIT_SELECT_ALL)
                Item(sel.title, onClick = { sel.execute(ActionContext()) }, enabled = sel.enabled())
                Separator()
                val fnd = registry.require(ActionIds.EDIT_FIND)
                Item(fnd.title, onClick = { fnd.execute(ActionContext()) }, enabled = fnd.enabled())
            }
        }

        App(
            selectedDir = selectedDir.value,
            recipes = recipes.value,
            selectedFilename = selectedFilename.value,
            selectedFileUrl = selectedFileUrl.value,
            selectedHtml = selectedHtml.value,
            webViewReady = webViewReady.value,
            restartRequired = restartRequired.value,
            indexLoading = indexLoading.value,
            isEditing = isEditing.value,
            onOpenRepo = ::pickDirectory,
            onSelectRecipe = ::selectRecipe,
            onHtmlChange = { selectedHtml.value = it },
        )
    }
}

private fun loadRecipeIndex(repo: FileSystemRecipeRepository): List<RecipeListItem> {
    return repo.listRecipes().map { filename ->
        val title = try {
            repo.loadRecipe(filename).title.ifBlank { stripHtmlExtension(filename) }
        } catch (_: Exception) {
            stripHtmlExtension(filename)
        }
        RecipeListItem(filename = filename, title = title)
    }
}

private fun stripHtmlExtension(filename: String): String =
    if (filename.endsWith(".html", ignoreCase = true)) {
        filename.dropLast(5)
    } else {
        filename
    }
