package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
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
import recipejar.macro.MacroDefinition
import recipejar.macro.MacroIo
import recipejar.macro.MacroProcessor
import recipejar.macro.MacroStore
import recipejar.persistence.FileSystemRecipeRepository
import recipejar.search.SearchScope
import java.awt.Color
import java.io.File
import javax.swing.JColorChooser
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

/**
 * Desktop entry: open a recipe repository directory, list via FileSystemRecipeRepository,
 * drive Rolodex-edge alpha index + reader, menus from ActionRegistry, macros, search, prefs.
 *
 * **Menus (hybrid):** native Compose [MenuBar] on macOS (screen menu bar); Material in-window
 * menus on Windows/Linux so chrome matches Material3 content.
 *
 * KCEF bootstrap enables compose-webview-multiplatform for file:// recipe HTML.
 * Install/cache live under `~/.cache/recipejar/` (stable, not CWD). If init fails or is still
 * in progress, App falls back to scrollable HTML text with a status banner.
 *
 * OS hooks ([Platform]): Cmd vs Ctrl shortcuts, reserved macOS accelerators, screen menu bar name.
 */

/** Stable KCEF install/cache root (avoids CWD drift between Gradle run and packaged app). */
private fun kcefDataRoot(): File {
    val home = System.getProperty("user.home") ?: "."
    return File(home, ".cache/recipejar").also { it.mkdirs() }
}
fun main() {
    Platform.applyStartupProperties()
    application {
    val selectedDir = remember { mutableStateOf<String?>(null) }
    val recipes = remember { mutableStateOf<List<RecipeListItem>>(emptyList()) }
    val selectedFilename = remember { mutableStateOf<String?>(null) }
    val selectedHtml = remember { mutableStateOf<String?>(null) }
    val selectedFileUrl = remember { mutableStateOf<String?>(null) }
    /** Last HTML loaded/saved from disk; used for dirty detection (WebView vs buffer). */
    val lastDiskHtml = remember { mutableStateOf<String?>(null) }
    /**
     * True after File→New until first successful save.
     * Ensures [originalFilename] is always null so we never rename/hijack an existing file
     * (e.g. a prior on-disk Untitled.html).
     */
    val isUnsavedNew = remember { mutableStateOf(false) }
    val webViewReady = remember { mutableStateOf(false) }
    val restartRequired = remember { mutableStateOf(false) }
    /** Human-readable KCEF status for the top banner (null when ready / silent). */
    val webViewStatusText = remember { mutableStateOf<String?>("Initializing WebView (KCEF)…") }
    val indexLoading = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }
    val statusMessage = remember { mutableStateOf<String?>(null) }
    val forceCompactLayout = remember { mutableStateOf(AppPrefs.forceCompactLayout) }
    /** Loaded user macros for the current repository (JSON / legacy txt / defaults). */
    val macros = remember { mutableStateOf<List<MacroDefinition>>(emptyList()) }
    val showMacroManager = remember { mutableStateOf(false) }
    val showSearch = remember { mutableStateOf(false) }
    val searchScopes = remember {
        mutableStateOf(setOf(SearchScope.TITLES, SearchScope.LABELS))
    }
    val showPreferences = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    /** Native AWT menu on macOS; Material in-window menus elsewhere. */
    val useNativeMenuBar = Platform.isMac

    val isRecipeOpen: () -> Boolean = { selectedFilename.value != null }
    fun isDirtyBuffer(): Boolean =
        isUnsavedNew.value ||
            (selectedHtml.value != null && selectedHtml.value != lastDiskHtml.value)

    fun setWebViewReadyOnMain(ready: Boolean) {
        scope.launch(Dispatchers.Main.immediate) {
            webViewReady.value = ready
            if (ready) {
                webViewStatusText.value = null
                restartRequired.value = false
            }
        }
    }

    fun setWebViewStatusOnMain(text: String?) {
        scope.launch(Dispatchers.Main.immediate) {
            webViewStatusText.value = text
        }
    }

    fun setRestartRequiredOnMain(required: Boolean) {
        scope.launch(Dispatchers.Main.immediate) {
            restartRequired.value = required
            if (required) {
                webViewReady.value = false
                webViewStatusText.value =
                    "WebView installed — restart RecipeJar to enable rendered recipes."
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val root = kcefDataRoot()
            val installDir = File(root, "kcef-bundle")
            val cacheDir = File(root, "kcef-cache")
            cacheDir.mkdirs()
            Debug.log("KCEF installDir=${installDir.absolutePath}")
            try {
                KCEF.init(builder = {
                    installDir(installDir)
                    progress {
                        onLocating {
                            setWebViewStatusOnMain("WebView: locating CEF runtime…")
                        }
                        onDownloading { percent ->
                            val p = percent.coerceIn(0f, 100f)
                            setWebViewStatusOnMain(
                                "WebView: downloading CEF… ${p.toInt()}% (first run may take a few minutes)",
                            )
                        }
                        onExtracting {
                            setWebViewStatusOnMain("WebView: extracting CEF…")
                        }
                        onInstall {
                            setWebViewStatusOnMain("WebView: installing CEF…")
                        }
                        onInitializing {
                            setWebViewStatusOnMain("WebView: initializing…")
                        }
                        onInitialized {
                            setWebViewReadyOnMain(true)
                            Debug.log("KCEF initialized")
                        }
                    }
                    settings {
                        cachePath = cacheDir.absolutePath
                    }
                }, onError = { err ->
                    Debug.error("KCEF init error", err)
                    setWebViewReadyOnMain(false)
                    val msg = err?.message?.takeIf { it.isNotBlank() } ?: err?.toString() ?: "unknown error"
                    setWebViewStatusOnMain("WebView unavailable ($msg). Showing HTML source.")
                }, onRestartRequired = {
                    Debug.log("KCEF restart required after install")
                    setRestartRequiredOnMain(true)
                })
            } catch (t: Throwable) {
                Debug.error("KCEF bootstrap failed", t)
                withContext(Dispatchers.Main) {
                    webViewReady.value = false
                    webViewStatusText.value = when (t) {
                        is UnsupportedClassVersionError ->
                            "WebView requires Java 17+ (KCEF). Current runtime is too old. Showing HTML source."
                        else ->
                            "WebView bootstrap failed (${t.message ?: t::class.simpleName}). Showing HTML source."
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                KCEF.disposeBlocking()
            } catch (t: Throwable) {
                Debug.error("KCEF dispose failed", t)
            }
        }
    }

    /**
     * Load repository at [path]: catalog, macros, prefs.
     *
     * When [restoreLastRecipe] is true, re-selects [AppPrefs.lastRecipeFor] for this absolute
     * path only (launch restore). When false (picker / prefs switch), opens with no selection
     * and leaves the scoped last-recipe key intact for a future restore.
     */
    fun openRepository(path: String, restoreLastRecipe: Boolean = true) {
        val abs = AppPrefs.normalizeRepoPath(path)
        if (abs == null) {
            Debug.error("Not a directory: $path")
            statusMessage.value = "Not a directory: $path"
            return
        }
        selectedDir.value = abs
        selectedFilename.value = null
        selectedHtml.value = null
        selectedFileUrl.value = null
        lastDiskHtml.value = null
        isUnsavedNew.value = false
        isEditing.value = false
        statusMessage.value = null
        recipes.value = emptyList()
        macros.value = emptyList()
        indexLoading.value = true
        // Only persist a validated absolute directory (never a bad typed path).
        AppPrefs.lastRepoPath = abs
        Debug.log("Opening repository: $abs")
        val want = if (restoreLastRecipe) AppPrefs.lastRecipeFor(abs) else null
        scope.launch {
            try {
                val (loaded, macroLoad) = withContext(Dispatchers.IO) {
                    val repo = FileSystemRecipeRepository(abs)
                    loadRecipeIndex(repo) to MacroStore.load(abs)
                }
                if (selectedDir.value != abs) return@launch
                recipes.value = loaded
                macros.value = macroLoad.macros
                if (macroLoad.note != null) {
                    statusMessage.value = macroLoad.note
                }
                if (want != null && loaded.any { it.filename == want }) {
                    // Inline restore (selectRecipe is defined below; avoid forward-ref).
                    selectedFilename.value = want
                    isEditing.value = false
                    isUnsavedNew.value = false
                    val file = File(abs, want)
                    if (file.isFile) {
                        selectedFileUrl.value = file.toURI().toString()
                        val html = withContext(Dispatchers.IO) {
                            try {
                                file.readText(Charsets.UTF_8)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (selectedDir.value == abs && selectedFilename.value == want) {
                            selectedHtml.value = html
                            lastDiskHtml.value = html
                            if (html == null) {
                                selectedFileUrl.value = null
                            } else {
                                AppPrefs.setLastRecipe(abs, want)
                            }
                        }
                    } else {
                        AppPrefs.setLastRecipe(abs, null)
                    }
                } else if (want != null) {
                    // Stale scoped filename for this repo only — do not touch other repos.
                    AppPrefs.setLastRecipe(abs, null)
                }
                // restoreLastRecipe == false: do not clear scoped last for this path
            } catch (e: Exception) {
                Debug.error("Failed to load repository: $abs", e)
                if (selectedDir.value == abs) {
                    recipes.value = emptyList()
                    macros.value = MacroIo.DEFAULT_MACROS
                    statusMessage.value = "Load failed: ${e.message}"
                }
            } finally {
                if (selectedDir.value == abs) {
                    indexLoading.value = false
                }
            }
        }
    }

    fun pickDirectory() {
        val start = AppPrefs.normalizeRepoPath(AppPrefs.lastRepoPath)
            ?.let { File(it) }
            ?: FileSystemView.getFileSystemView().homeDirectory
        val chooser = JFileChooser(start)
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Open recipe repository"
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val dir = chooser.selectedFile
            if (dir != null && dir.isDirectory) {
                // Explicit open: no auto-select this session (scoped last kept for later)
                openRepository(dir.absolutePath, restoreLastRecipe = false)
            }
        }
    }

    // Restore last repository on launch (scoped last recipe re-selected inside openRepository).
    LaunchedEffect(Unit) {
        val last = AppPrefs.normalizeRepoPath(AppPrefs.lastRepoPath)
        if (last != null) {
            openRepository(last, restoreLastRecipe = true)
        }
    }

    /**
     * Apply a macro template to the editor buffer.
     *
     * MVP (no caret/selection API on BasicTextField):
     * - Templates with [SELECTION]: expand with the **whole buffer** as selection, replace buffer.
     * - Templates without [SELECTION]: **append** expansion (insert-only; avoids wiping the recipe).
     * - Cancelled INPUT/COLOR → no buffer mutation.
     */
    fun applyMacroToBuffer(macro: MacroDefinition) {
        if (!isEditing.value) {
            statusMessage.value = "Macros apply in edit mode (Recipe → Toggle Edit)"
            return
        }
        val html = selectedHtml.value
        if (html == null) {
            statusMessage.value = "No recipe buffer open"
            return
        }
        val usesSelection = MacroProcessor.containsSelectionPlaceholder(macro.text)
        val result = MacroProcessor.applyMacro(
            template = macro.text,
            selection = if (usesSelection) html else "",
            inputProvider = { prompt ->
                JOptionPane.showInputDialog(null, prompt, macro.name, JOptionPane.QUESTION_MESSAGE)
            },
            colorProvider = { prompt ->
                val c = JColorChooser.showDialog(null, prompt.ifBlank { "Select Color" }, Color.BLACK)
                if (c == null) {
                    null
                } else {
                    val r = c.red.toString(16).uppercase().padStart(2, '0')
                    val g = c.green.toString(16).uppercase().padStart(2, '0')
                    val b = c.blue.toString(16).uppercase().padStart(2, '0')
                    "#$r$g$b"
                }
            },
        )
        if (result == null) {
            statusMessage.value = "Macro cancelled: ${macro.name}"
            return
        }
        selectedHtml.value = if (usesSelection) result else html + result
        statusMessage.value = if (usesSelection) {
            "Applied macro: ${macro.name} (full buffer as selection)"
        } else {
            "Applied macro: ${macro.name} (appended)"
        }
    }

    /**
     * @return parsed macros, or null if cancelled / unreadable (caller must not wipe list).
     */
    fun importMacrosTxtFile(): List<MacroDefinition>? {
        val chooser = JFileChooser(
            selectedDir.value?.let { File(it) }
                ?: FileSystemView.getFileSystemView().homeDirectory,
        )
        chooser.dialogTitle = "Import macros.txt"
        chooser.fileFilter = FileNameExtensionFilter("Macro text (*.txt)", "txt")
        val result = chooser.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION) return null
        val file = chooser.selectedFile ?: return null
        return try {
            MacroStore.importTxt(file.absolutePath)
        } catch (_: Exception) {
            statusMessage.value = "Macro import failed: could not read ${file.name}"
            null
        }
    }

    fun saveMacros(list: List<MacroDefinition>) {
        val dir = selectedDir.value
        if (dir == null) {
            statusMessage.value = "Open a repository before saving macros"
            return
        }
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    MacroStore.save(dir, list)
                }
                // Repo may have switched while save was in flight — do not clobber new session.
                if (selectedDir.value != dir) {
                    return@launch
                }
                macros.value = list
                showMacroManager.value = false
                statusMessage.value = "Saved ${list.size} macro(s) to ${MacroIo.JSON_FILENAME}"
            } catch (e: Exception) {
                if (selectedDir.value == dir) {
                    statusMessage.value = "Macro save failed: ${e.message}"
                }
            }
        }
    }

    fun selectRecipe(filename: String) {
        val dir = selectedDir.value ?: return
        selectedFilename.value = filename
        isEditing.value = false
        isUnsavedNew.value = false
        statusMessage.value = null
        val file = File(dir, filename)
        if (!file.isFile) {
            selectedFileUrl.value = null
            selectedHtml.value = null
            lastDiskHtml.value = null
            Debug.error("Recipe file missing: $filename")
            return
        }
        // Persist last recipe only after a successful bind to an on-disk file in this repo.
        AppPrefs.setLastRecipe(dir, filename)
        selectedFileUrl.value = file.toURI().toString()
        selectedHtml.value = null
        lastDiskHtml.value = null
        scope.launch {
            val html = withContext(Dispatchers.IO) {
                try {
                    file.readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    Debug.error("Failed to read $filename", e)
                    null
                }
            }
            if (selectedFilename.value != filename || selectedDir.value != dir) return@launch
            if (html == null) {
                selectedFileUrl.value = null
                selectedHtml.value = null
                lastDiskHtml.value = null
            } else {
                selectedHtml.value = html
                lastDiskHtml.value = html
            }
        }
    }

    /** Build searchable field map for the current repo (labels, notes, ingredients, procedure). */
    fun buildSearchFieldText(): Map<String, Map<SearchScope, String>> {
        val dir = selectedDir.value ?: return emptyMap()
        return try {
            val repo = FileSystemRecipeRepository(dir)
            val out = mutableMapOf<String, Map<SearchScope, String>>()
            for (name in repo.listRecipes()) {
                try {
                    val r = repo.loadRecipe(name)
                    out[name] = mapOf(
                        SearchScope.LABELS to r.getLabelsAsString(),
                        SearchScope.NOTES to r.notes,
                        SearchScope.INGREDIENTS to r.ingredients.joinToString(" ") { it.toString() },
                        SearchScope.PROCEDURE to r.procedure,
                    )
                } catch (e: Exception) {
                    Debug.error("Search index skip: $name", e)
                }
            }
            out
        } catch (e: Exception) {
            Debug.error("Search field index failed", e)
            emptyMap()
        }
    }

    fun openSearch(scopes: Set<SearchScope>) {
        if (selectedDir.value == null) {
            statusMessage.value = "Open a repository to search"
            return
        }
        searchScopes.value = scopes
        showSearch.value = true
    }

    /**
     * Persist editor buffer via [FileSystemRecipeRepository].
     * Parses HTML, serializes with browser-footer; renames only when bound to an existing on-disk file
     * (never for File→New until after first successful save clears [isUnsavedNew]).
     */
    fun saveCurrentRecipe() {
        val dir = selectedDir.value ?: return
        val filename = selectedFilename.value ?: return
        val html = selectedHtml.value ?: return
        if (html.isBlank()) {
            statusMessage.value = "Save: empty buffer"
            return
        }
        // Capture at start for in-flight navigation checks
        val saveTargetFilename = filename
        val wasUnsavedNew = isUnsavedNew.value
        scope.launch {
            try {
                val savedName = withContext(Dispatchers.IO) {
                    val repo = FileSystemRecipeRepository(dir)
                    val recipe = RecipeSerializer.parse(html)
                    if (recipe.title.isBlank()) {
                        recipe.title = stripHtmlExtension(filename).ifBlank { "Untitled" }
                    }
                    val author = AppPrefs.authorName
                    if (author.isNotBlank()) {
                        recipe.meta["author"] = author
                    }
                    // New buffers never pass originalFilename (avoid hijacking existing Untitled.html).
                    // Existing recipes pass on-disk name for title-rename support.
                    val original = if (wasUnsavedNew) {
                        null
                    } else {
                        filename.takeIf { File(dir, it).isFile }
                    }
                    repo.saveRecipe(recipe, originalFilename = original)
                    repo.filenameFor(recipe)
                }
                if (selectedDir.value != dir) return@launch

                // Always refresh catalog in this same coroutine (no nested launch).
                val loaded = withContext(Dispatchers.IO) {
                    try {
                        loadRecipeIndex(FileSystemRecipeRepository(dir))
                    } catch (e: Exception) {
                        Debug.error("Index refresh after save failed", e)
                        emptyList()
                    }
                }
                if (selectedDir.value != dir) return@launch
                recipes.value = loaded

                // Only reselect/reload if user is still on the document that was saved.
                // (Selecting another recipe or File→New while save was in flight must not clobber.)
                if (selectedFilename.value != saveTargetFilename) {
                    statusMessage.value = "Saved $savedName"
                    Debug.log("Saved $savedName (selection moved)")
                    return@launch
                }

                isUnsavedNew.value = false
                selectedFilename.value = savedName
                AppPrefs.setLastRecipe(dir, savedName)
                val file = File(dir, savedName)
                if (file.isFile) {
                    selectedFileUrl.value = file.toURI().toString()
                    val diskHtml = withContext(Dispatchers.IO) {
                        try {
                            file.readText(Charsets.UTF_8)
                        } catch (e: Exception) {
                            Debug.error("Re-read after save failed: $savedName", e)
                            null
                        }
                    }
                    if (selectedDir.value == dir && selectedFilename.value == savedName) {
                        if (diskHtml != null) {
                            selectedHtml.value = diskHtml
                            lastDiskHtml.value = diskHtml
                            statusMessage.value = "Saved $savedName"
                            Debug.log("Saved $savedName")
                        } else {
                            selectedFileUrl.value = null
                            selectedHtml.value = null
                            lastDiskHtml.value = null
                            statusMessage.value = "Saved $savedName but could not re-read file"
                        }
                    }
                } else {
                    selectedFileUrl.value = null
                    selectedHtml.value = null
                    lastDiskHtml.value = null
                    statusMessage.value = "Saved as $savedName but file is missing"
                    Debug.error("Saved path missing: $savedName")
                }
            } catch (e: Exception) {
                Debug.error("Save failed", e)
                statusMessage.value = "Save failed: ${e.message}"
            }
        }
    }

    fun newRecipe() {
        val recipe = Recipe(
            title = "Untitled",
            notes = "A little about this recipe,<br/>\nwhere I got it why I like it, etc.<br/>\nMakes 1 serving.",
            procedure = "",
        )
        val html = RecipeSerializer.serialize(recipe, "browser-footer")
        // Provisional display name only; isUnsavedNew forces originalFilename=null on first save.
        selectedFilename.value = "Untitled.html"
        selectedHtml.value = html
        selectedFileUrl.value = null
        lastDiskHtml.value = null
        isUnsavedNew.value = true
        isEditing.value = true
        statusMessage.value = "New recipe (unsaved)"
    }

    val registry = remember {
        ActionRegistry().also { reg ->
            val isOpen = isRecipeOpen

            reg.register(
                ActionIds.FILE_NEW,
                Command(
                    id = ActionIds.FILE_NEW,
                    title = "New",
                    mnemonic = 'N',
                    shortcut = Platform.allowedShortcut(Platform.primaryShortcut('N')),
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
                    shortcut = Platform.allowedShortcut(Platform.primaryShortcut('O')),
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
                    shortcut = Platform.allowedShortcut(Platform.primaryShortcut('S')),
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
                        Debug.log("Delete stub: ${selectedFilename.value}")
                        selectedFilename.value = null
                        selectedHtml.value = null
                        selectedFileUrl.value = null
                        lastDiskHtml.value = null
                        isUnsavedNew.value = false
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
                        } ?: Debug.log("Rename: no current recipe open")
                    },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_IMPORT,
                Command(
                    id = ActionIds.FILE_IMPORT,
                    title = "Import",
                    execute = { Debug.log("Import stub") },
                    enabled = { selectedDir.value != null }
                )
            )

            reg.register(
                ActionIds.FILE_EXPORT,
                Command(
                    id = ActionIds.FILE_EXPORT,
                    title = "Export",
                    execute = { Debug.log("Export stub for: ${selectedFilename.value}") },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_PRINT,
                Command(
                    id = ActionIds.FILE_PRINT,
                    title = "Print",
                    mnemonic = 'P',
                    shortcut = Platform.allowedShortcut(Platform.primaryShortcut('P')),
                    execute = { Debug.log("Print stub") },
                    enabled = { false }
                )
            )

            // On macOS, system Quit is Cmd+Q (reserved); keep in-app Exit without claiming Cmd+Q.
            reg.register(
                ActionIds.FILE_EXIT,
                Command(
                    id = ActionIds.FILE_EXIT,
                    title = if (Platform.isMac) "Quit RecipeJar" else "Exit",
                    mnemonic = 'X',
                    execute = { exitApplication() }
                )
            )

            val editEnabled = isOpen
            reg.register(ActionIds.EDIT_CUT, Command(id = ActionIds.EDIT_CUT, title = "Cut", execute = { Debug.log("Cut stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_COPY, Command(id = ActionIds.EDIT_COPY, title = "Copy", execute = { Debug.log("Copy stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_PASTE, Command(id = ActionIds.EDIT_PASTE, title = "Paste", execute = { Debug.log("Paste stub") }, enabled = editEnabled))
            reg.register(ActionIds.EDIT_SELECT_ALL, Command(id = ActionIds.EDIT_SELECT_ALL, title = "Select All", execute = { Debug.log("SelectAll stub") }, enabled = editEnabled))
            reg.register(
                ActionIds.EDIT_MACROS,
                Command(
                    id = ActionIds.EDIT_MACROS,
                    title = "Manage Macros…",
                    execute = {
                        if (selectedDir.value == null) {
                            statusMessage.value = "Open a repository to manage macros"
                        } else {
                            showMacroManager.value = true
                        }
                    },
                    enabled = { selectedDir.value != null },
                )
            )
            val allScopes = setOf(
                SearchScope.TITLES,
                SearchScope.LABELS,
                SearchScope.NOTES,
                SearchScope.INGREDIENTS,
                SearchScope.PROCEDURE,
            )
            reg.register(
                ActionIds.EDIT_FIND,
                Command(
                    id = ActionIds.EDIT_FIND,
                    title = "Find…",
                    mnemonic = 'F',
                    shortcut = Platform.allowedShortcut(Platform.primaryShortcut('F')),
                    execute = { openSearch(setOf(SearchScope.TITLES, SearchScope.LABELS)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_ALL,
                Command(
                    id = ActionIds.FIND_ALL,
                    title = "Find All",
                    execute = { openSearch(allScopes) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_TITLES,
                Command(
                    id = ActionIds.FIND_TITLES,
                    title = "Find Titles",
                    execute = { openSearch(setOf(SearchScope.TITLES)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_LABELS,
                Command(
                    id = ActionIds.FIND_LABELS,
                    title = "Find Labels",
                    execute = { openSearch(setOf(SearchScope.LABELS)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_NOTES,
                Command(
                    id = ActionIds.FIND_NOTES,
                    title = "Find Notes",
                    execute = { openSearch(setOf(SearchScope.NOTES)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_INGREDIENTS,
                Command(
                    id = ActionIds.FIND_INGREDIENTS,
                    title = "Find Ingredients",
                    execute = { openSearch(setOf(SearchScope.INGREDIENTS)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.FIND_PROCEDURES,
                Command(
                    id = ActionIds.FIND_PROCEDURES,
                    title = "Find Procedures",
                    execute = { openSearch(setOf(SearchScope.PROCEDURE)) },
                    enabled = { selectedDir.value != null },
                )
            )
            reg.register(
                ActionIds.TOOLS_PREFERENCES,
                Command(
                    id = ActionIds.TOOLS_PREFERENCES,
                    // Do not bind Cmd+, (reserved on macOS); menu title only.
                    title = "Preferences…",
                    execute = { showPreferences.value = true },
                )
            )
            reg.register(
                ActionIds.HELP_ABOUT,
                Command(
                    id = ActionIds.HELP_ABOUT,
                    title = "About RecipeJar",
                    mnemonic = 'A',
                    execute = {
                        JOptionPane.showMessageDialog(
                            null,
                            Platform.APP_ABOUT,
                            "About ${Platform.APP_NAME}",
                            JOptionPane.INFORMATION_MESSAGE,
                        )
                    },
                )
            )
        }
    }

    /**
     * Re-register dynamic macro.* commands when the list changes (reload without restart).
     * Menu items read [macros] state directly; registry ids support future command-palette use.
     */
    fun registerMacroCommands(list: List<MacroDefinition>) {
        registry.clearPrefix(ActionIds.MACRO_PREFIX)
        for (macro in list) {
            var base = registry.sanitizeId(macro.name).ifEmpty { "unnamed" }
            var id = ActionIds.MACRO_PREFIX + base
            var suffix = 2
            while (registry.find(id) != null) {
                id = ActionIds.MACRO_PREFIX + base + suffix
                suffix++
            }
            val captured = macro
            registry.register(
                id,
                Command(
                    id = id,
                    title = captured.name,
                    execute = { applyMacroToBuffer(captured) },
                    enabled = { isEditing.value && selectedHtml.value != null },
                    mnemonic = captured.mnemonic?.firstOrNull(),
                )
            )
        }
    }

    LaunchedEffect(macros.value) {
        registerMacroCommands(macros.value)
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
            'F' -> Key.F
            else -> Key.Unknown
        }
        return KeyShortcut(key = k, ctrl = combo.ctrl, meta = combo.meta, alt = combo.alt, shift = combo.shift)
    }

    fun runCommand(id: String) {
        val c = registry.find(id) ?: return
        if (c.enabled()) c.execute(ActionContext())
    }

    fun toggleForceCompact() {
        val next = !forceCompactLayout.value
        forceCompactLayout.value = next
        AppPrefs.forceCompactLayout = next
        statusMessage.value = if (next) "Phone layout on" else "Phone layout off"
    }

    fun clearSelection() {
        selectedFilename.value = null
        selectedHtml.value = null
        selectedFileUrl.value = null
        lastDiskHtml.value = null
        isUnsavedNew.value = false
        isEditing.value = false
    }

    /**
     * Material menu model for non-macOS. Rebuilt when macros / edit / selection enablement change.
     */
    fun buildMaterialMenus(): AppMenuModel {
        fun item(id: String) = registry.require(id).let { c ->
            AppMenuEntry.Item(
                title = c.title,
                enabled = c.enabled(),
                onClick = { c.execute(ActionContext()) },
            )
        }
        val macroEntries: List<AppMenuEntry> = buildList {
            val macroList = macros.value
            if (macroList.isEmpty()) {
                add(AppMenuEntry.Item("(no macros — open a repository)", enabled = false, onClick = {}))
            } else {
                macroList.forEach { macro ->
                    add(
                        AppMenuEntry.Item(
                            title = macro.name,
                            enabled = isEditing.value && selectedHtml.value != null,
                            onClick = { applyMacroToBuffer(macro) },
                        ),
                    )
                }
            }
            add(AppMenuEntry.Separator)
            add(item(ActionIds.EDIT_MACROS))
        }
        return AppMenuModel(
            menus = listOf(
                AppMenu(
                    "Recipe",
                    listOf(
                        item(ActionIds.FILE_NEW),
                        item(ActionIds.FILE_TOGGLE_EDIT),
                        item(ActionIds.FILE_SAVE),
                        item(ActionIds.FILE_RENAME),
                        AppMenuEntry.Separator,
                        item(ActionIds.FILE_IMPORT),
                        item(ActionIds.FILE_EXPORT),
                        AppMenuEntry.Separator,
                        item(ActionIds.FILE_DELETE),
                        AppMenuEntry.Separator,
                        item(ActionIds.FILE_EXIT),
                    ),
                ),
                AppMenu(
                    "Edit",
                    listOf(
                        item(ActionIds.EDIT_CUT),
                        item(ActionIds.EDIT_COPY),
                        item(ActionIds.EDIT_PASTE),
                        item(ActionIds.EDIT_SELECT_ALL),
                        AppMenuEntry.Separator,
                        item(ActionIds.EDIT_FIND),
                    ),
                ),
                AppMenu(
                    "Find",
                    listOf(
                        item(ActionIds.FIND_ALL),
                        item(ActionIds.FIND_TITLES),
                        item(ActionIds.FIND_LABELS),
                        item(ActionIds.FIND_NOTES),
                        item(ActionIds.FIND_INGREDIENTS),
                        item(ActionIds.FIND_PROCEDURES),
                    ),
                ),
                AppMenu("Macros", macroEntries),
                AppMenu(
                    "Tools",
                    listOf(
                        item(ActionIds.TOOLS_PREFERENCES),
                        AppMenuEntry.Item(
                            title = if (forceCompactLayout.value) "Phone layout ✓" else "Phone layout",
                            onClick = { toggleForceCompact() },
                        ),
                    ),
                ),
                AppMenu(
                    "Help",
                    listOf(item(ActionIds.HELP_ABOUT)),
                ),
            ),
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "RecipeJar",
        onPreviewKeyEvent = { event ->
            // Material menu path has no native MenuBar shortcuts — handle primary keys here.
            if (useNativeMenuBar) return@Window false
            if (event.type != KeyEventType.KeyDown) return@Window false
            val primary = if (Platform.isMac) event.isMetaPressed else event.isCtrlPressed
            if (!primary && event.key != Key.Delete) return@Window false
            when {
                primary && event.key == Key.N -> {
                    runCommand(ActionIds.FILE_NEW); true
                }
                primary && event.key == Key.O -> {
                    runCommand(ActionIds.FILE_TOGGLE_EDIT); true
                }
                primary && event.key == Key.S -> {
                    runCommand(ActionIds.FILE_SAVE); true
                }
                primary && event.key == Key.F -> {
                    runCommand(ActionIds.EDIT_FIND); true
                }
                event.key == Key.Delete -> {
                    runCommand(ActionIds.FILE_DELETE); true
                }
                else -> false
            }
        },
    ) {
        // Hybrid: native screen/AWT menu bar on macOS only.
        if (useNativeMenuBar) {
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
                    Item(fnd.title, onClick = { fnd.execute(ActionContext()) }, enabled = fnd.enabled(), shortcut = toKeyShortcut(fnd.shortcut))
                }
                Menu("Find", mnemonic = 'F') {
                    val fa = registry.require(ActionIds.FIND_ALL)
                    Item(fa.title, onClick = { fa.execute(ActionContext()) }, enabled = fa.enabled())
                    val ft = registry.require(ActionIds.FIND_TITLES)
                    Item(ft.title, onClick = { ft.execute(ActionContext()) }, enabled = ft.enabled())
                    val fl = registry.require(ActionIds.FIND_LABELS)
                    Item(fl.title, onClick = { fl.execute(ActionContext()) }, enabled = fl.enabled())
                    val fn = registry.require(ActionIds.FIND_NOTES)
                    Item(fn.title, onClick = { fn.execute(ActionContext()) }, enabled = fn.enabled())
                    val fi = registry.require(ActionIds.FIND_INGREDIENTS)
                    Item(fi.title, onClick = { fi.execute(ActionContext()) }, enabled = fi.enabled())
                    val fp = registry.require(ActionIds.FIND_PROCEDURES)
                    Item(fp.title, onClick = { fp.execute(ActionContext()) }, enabled = fp.enabled())
                }
                Menu("Macros", mnemonic = 'M') {
                    val macroList = macros.value
                    if (macroList.isEmpty()) {
                        Item("(no macros — open a repository)", onClick = {}, enabled = false)
                    } else {
                        macroList.forEach { macro ->
                            Item(
                                macro.name,
                                onClick = { applyMacroToBuffer(macro) },
                                enabled = isEditing.value && selectedHtml.value != null,
                            )
                        }
                    }
                    Separator()
                    val mgr = registry.require(ActionIds.EDIT_MACROS)
                    Item(mgr.title, onClick = { mgr.execute(ActionContext()) }, enabled = mgr.enabled())
                }
                Menu("Tools", mnemonic = 'T') {
                    val pref = registry.require(ActionIds.TOOLS_PREFERENCES)
                    Item(pref.title, onClick = { pref.execute(ActionContext()) }, enabled = pref.enabled())
                    Item(
                        if (forceCompactLayout.value) "Phone layout ✓" else "Phone layout",
                        onClick = { toggleForceCompact() },
                    )
                }
                Menu("Help", mnemonic = 'H') {
                    val about = registry.require(ActionIds.HELP_ABOUT)
                    Item(about.title, onClick = { about.execute(ActionContext()) }, enabled = about.enabled())
                }
            }
        }

        // Dirty buffer: do not feed WebView a stale file:// URL; reader falls back to selectedHtml.
        val readerFileUrl = if (isDirtyBuffer()) null else selectedFileUrl.value

        // Rebuild material menus when enablement-related state changes.
        val materialMenus = if (!useNativeMenuBar) {
            // Touch state so Compose recomposes when enablement changes.
            @Suppress("UNUSED_EXPRESSION")
            selectedFilename.value
            @Suppress("UNUSED_EXPRESSION")
            isEditing.value
            @Suppress("UNUSED_EXPRESSION")
            macros.value
            @Suppress("UNUSED_EXPRESSION")
            forceCompactLayout.value
            @Suppress("UNUSED_EXPRESSION")
            selectedDir.value
            buildMaterialMenus()
        } else {
            null
        }

        App(
            selectedDir = selectedDir.value,
            recipes = recipes.value,
            selectedFilename = selectedFilename.value,
            selectedFileUrl = readerFileUrl,
            selectedHtml = selectedHtml.value,
            webViewReady = webViewReady.value,
            restartRequired = restartRequired.value,
            webViewStatusText = webViewStatusText.value,
            indexLoading = indexLoading.value,
            isEditing = isEditing.value,
            statusMessage = statusMessage.value,
            materialMenus = materialMenus,
            forceCompactLayout = forceCompactLayout.value,
            onForceCompactChange = { next ->
                forceCompactLayout.value = next
                AppPrefs.forceCompactLayout = next
            },
            onOpenRepo = ::pickDirectory,
            onSelectRecipe = ::selectRecipe,
            onHtmlChange = { selectedHtml.value = it },
            onClearSelection = ::clearSelection,
        )

        if (showMacroManager.value) {
            MacroManagerDialog(
                initial = macros.value,
                onSave = { list -> saveMacros(list) },
                onDismiss = { showMacroManager.value = false },
                onImportTxt = { importMacrosTxtFile() },
            )
        }

        if (showSearch.value) {
            SearchDialog(
                recipes = recipes.value,
                initialScopes = searchScopes.value,
                fieldTextProvider = {
                    // Disk + parse off the UI thread; dialog shows "Indexing…".
                    withContext(Dispatchers.IO) { buildSearchFieldText() }
                },
                onSelect = { filename -> selectRecipe(filename) },
                onDismiss = { showSearch.value = false },
            )
        }

        if (showPreferences.value) {
            PreferencesDialog(
                initialRepoPath = selectedDir.value ?: AppPrefs.lastRepoPath.orEmpty(),
                initialAuthorName = AppPrefs.authorName,
                onBrowseRepo = {
                    val start = AppPrefs.normalizeRepoPath(selectedDir.value ?: AppPrefs.lastRepoPath)
                        ?.let { File(it) }
                        ?: FileSystemView.getFileSystemView().homeDirectory
                    val chooser = JFileChooser(start)
                    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    chooser.dialogTitle = "Recipe repository"
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        chooser.selectedFile?.let { AppPrefs.normalizeRepoPath(it.absolutePath) }
                    } else {
                        null
                    }
                },
                onSave = { path, author ->
                    AppPrefs.authorName = author
                    when {
                        path.isBlank() -> {
                            // Intentional: forget last repo without closing the open session.
                            AppPrefs.lastRepoPath = null
                            statusMessage.value = "Preferences saved (last repository cleared)"
                            Debug.log("Preferences saved; lastRepoPath cleared")
                            null
                        }
                        else -> {
                            val abs = AppPrefs.normalizeRepoPath(path)
                            if (abs == null) {
                                // Do not clobber a good lastRepoPath with an invalid path.
                                Debug.log("Prefs path invalid (not persisted): $path")
                                "Not a directory: $path"
                            } else {
                                AppPrefs.lastRepoPath = abs
                                if (selectedDir.value != abs) {
                                    openRepository(abs, restoreLastRecipe = false)
                                }
                                statusMessage.value = "Preferences saved"
                                Debug.log("Preferences saved (repo=$abs, author=${author.isNotBlank()})")
                                null
                            }
                        }
                    }
                },
                onDismiss = { showPreferences.value = false },
            )
        }
    }
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
