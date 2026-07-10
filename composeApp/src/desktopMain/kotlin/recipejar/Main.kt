package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import recipejar.persistence.FileSystemRecipeRepository
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView
import kotlin.math.max

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

    // KCEF first-run may download CEF binaries; failure must not block the app.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading {
                            // progress reserved for future status UI
                            max(it, 0f)
                        }
                        onInitialized {
                            webViewReady.value = true
                        }
                    }
                    settings {
                        cachePath = File("kcef-cache").absolutePath
                    }
                }, onError = {
                    webViewReady.value = false
                }, onRestartRequired = {
                    webViewReady.value = false
                })
            } catch (_: Throwable) {
                webViewReady.value = false
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
                selectedDir.value = dir.absolutePath
                selectedFilename.value = null
                selectedHtml.value = null
                selectedFileUrl.value = null
                val repo = FileSystemRecipeRepository(dir.absolutePath)
                recipes.value = loadRecipeIndex(repo)
            }
        }
    }

    fun selectRecipe(filename: String) {
        val dir = selectedDir.value ?: return
        selectedFilename.value = filename
        val file = File(dir, filename)
        selectedFileUrl.value = file.toURI().toString()
        selectedHtml.value = try {
            if (file.isFile) file.readText(Charsets.UTF_8) else null
        } catch (_: Exception) {
            null
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
            onOpenRepo = ::pickDirectory,
            onSelectRecipe = ::selectRecipe,
        )
    }
}

/**
 * Build alpha-index entries: list filenames via repo, load titles via loadRecipe.
 * Skips files that fail to parse (title falls back to filename).
 */
private fun loadRecipeIndex(repo: FileSystemRecipeRepository): List<RecipeListItem> {
    return repo.listRecipes().map { filename ->
        val title = try {
            repo.loadRecipe(filename).title.ifBlank { filename.removeSuffix(".html") }
        } catch (_: Exception) {
            filename.removeSuffix(".html")
        }
        RecipeListItem(filename = filename, title = title)
    }
}
