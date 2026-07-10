package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import recipejar.persistence.FileSystemRecipeRepository
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

/**
 * Desktop entry: open a recipe repository directory, list via FileSystemRecipeRepository,
 * and drive the alpha-tab index + reader shell.
 *
 * KCEF bootstrap enables compose-webview-multiplatform for file:// recipe HTML (CSS resolves).
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
    val scope = rememberCoroutineScope()

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

    // KCEF first-run may download CEF binaries; failure must not block the app.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading {
                            // progress reserved for future status UI
                        }
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
                // ignore when init never completed
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
                recipes.value = emptyList()
                indexLoading.value = true
                scope.launch {
                    val loaded = withContext(Dispatchers.IO) {
                        val repo = FileSystemRecipeRepository(path)
                        loadRecipeIndex(repo)
                    }
                    if (selectedDir.value == path) {
                        recipes.value = loaded
                        indexLoading.value = false
                    }
                }
            }
        }
    }

    fun selectRecipe(filename: String) {
        val dir = selectedDir.value ?: return
        selectedFilename.value = filename
        val file = File(dir, filename)
        if (file.isFile) {
            selectedFileUrl.value = file.toURI().toString()
            selectedHtml.value = try {
                file.readText(Charsets.UTF_8)
            } catch (_: Exception) {
                selectedFileUrl.value = null
                null
            }
        } else {
            selectedFileUrl.value = null
            selectedHtml.value = null
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "RecipeJar",
    ) {
        App(
            selectedDir = selectedDir.value,
            recipes = recipes.value,
            selectedFilename = selectedFilename.value,
            selectedFileUrl = selectedFileUrl.value,
            selectedHtml = selectedHtml.value,
            webViewReady = webViewReady.value,
            restartRequired = restartRequired.value,
            indexLoading = indexLoading.value,
            onOpenRepo = ::pickDirectory,
            onSelectRecipe = ::selectRecipe,
        )
    }
}

/**
 * Build alpha-index entries: list filenames via repo, load titles via loadRecipe.
 * On parse failure, use filename (sans .html) as title so the entry still appears in the index.
 */
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

/** Strip trailing `.html` ignoring case (listRecipes accepts any case extension). */
private fun stripHtmlExtension(filename: String): String =
    if (filename.endsWith(".html", ignoreCase = true)) {
        filename.dropLast(5)
    } else {
        filename
    }
