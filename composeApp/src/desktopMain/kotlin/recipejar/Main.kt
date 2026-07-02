package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

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
                // Basic file listing stub for repo (html or all). Future: integrate real repo loader.
                // TODO(PR2+): remember last dir (prefs), filter *.html only, error UX, start chooser at last path or repo default.
                val listed = dir.listFiles()?.filter { it.isFile }?.map { it.name }?.sorted() ?: emptyList()
                files.value = listed
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
