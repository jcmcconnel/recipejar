package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView
import recipejar.persistence.FileSystemRecipeRepository

fun main() = application {
    val selectedDir = remember { mutableStateOf<String?>(null) }
    val files = remember { mutableStateOf<List<String>>(emptyList()) }

    fun pickDirectory() {
        val chooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.dialogTitle = "Open recipe repository"
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val dir = chooser.selectedFile
            if (dir != null && dir.isDirectory) {
                selectedDir.value = dir.absolutePath
                // PR3: use FileSystemRecipeRepository (desktop actual) for list after dir pick integration.
                val repo = FileSystemRecipeRepository(dir.absolutePath)
                files.value = repo.listRecipes()
            }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "RecipeJar"
    ) {
        App(
            selectedDir = selectedDir.value,
            files = files.value,
            onOpenRepo = ::pickDirectory
        )
    }
}
