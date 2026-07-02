package recipejar

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView
import recipejar.actions.*

fun main() = application {
    val selectedDir = remember { mutableStateOf<String?>(null) }
    val files = remember { mutableStateOf<List<String>>(emptyList()) }
    val currentRecipe = remember { mutableStateOf<String?>(null) }
    val isRecipeOpen = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }

    val onSelectRecipe: (String) -> Unit = { name ->
        currentRecipe.value = name
        isRecipeOpen.value = true
        isEditing.value = false
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
                currentRecipe.value = null
                isRecipeOpen.value = false
                isEditing.value = false
                // Basic file listing stub for repo (html or all). Future: integrate real repo loader.
                // TODO(PR2+): remember last dir (prefs), filter *.html only, error UX, start chooser at last path or repo default.
                val listed = dir.listFiles()?.filter { it.isFile }?.map { it.name }?.sorted() ?: emptyList()
                files.value = listed
            }
        }
    }

    val registry = remember {
        ActionRegistry().also { reg ->
            val isMac = System.getProperty("os.name").lowercase().contains("mac")
            val isOpen: () -> Boolean = { isRecipeOpen.value }

            // Core file actions per PR5 (smallest port + stubs)
            reg.register(
                ActionIds.FILE_NEW,
                Command(
                    id = ActionIds.FILE_NEW,
                    title = "New",
                    mnemonic = 'N',
                    shortcut = KeyCombo('N', meta = isMac, ctrl = !isMac),
                    execute = {
                        currentRecipe.value = "Untitled"
                        isRecipeOpen.value = true
                        isEditing.value = true
                    }
                )
            )

            reg.register(
                ActionIds.FILE_TOGGLE_EDIT,
                Command(
                    id = ActionIds.FILE_TOGGLE_EDIT,
                    title = "Toggle Edit",
                    mnemonic = 'O',
                    shortcut = KeyCombo('O', meta = isMac, ctrl = !isMac),
                    execute = {
                        isEditing.value = !isEditing.value
                    },
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
                    execute = {
                        println("Save stub executed for: ${currentRecipe.value}")
                        // In full impl: persist via repo, update index, refresh (from PR2+)
                    },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_DELETE,
                Command(
                    id = ActionIds.FILE_DELETE,
                    title = "Remove",
                    mnemonic = 'R',
                    shortcut = KeyCombo(delete = true), // matches original VK_DELETE, 0 mods
                    execute = {
                        println("Delete stub: ${currentRecipe.value}")
                        currentRecipe.value = null
                        isRecipeOpen.value = false
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
                        currentRecipe.value?.let { cur ->
                            currentRecipe.value = "$cur-renamed"
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
                    execute = {
                        println("Import stub (full FS + repo in later PR)")
                    },
                    enabled = isOpen
                )
            )

            reg.register(
                ActionIds.FILE_EXPORT,
                Command(
                    id = ActionIds.FILE_EXPORT,
                    title = "Export",
                    execute = {
                        println("Export stub for: ${currentRecipe.value}")
                    },
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
                    execute = { println("Print stub (TODO)") },
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

            // Edit actions (stubs; no real editor focus yet)
            val editEnabled = isOpen
            reg.register(
                ActionIds.EDIT_CUT,
                Command(id = ActionIds.EDIT_CUT, title = "Cut", execute = { println("Cut stub") }, enabled = editEnabled)
            )
            reg.register(
                ActionIds.EDIT_COPY,
                Command(id = ActionIds.EDIT_COPY, title = "Copy", execute = { println("Copy stub") }, enabled = editEnabled)
            )
            reg.register(
                ActionIds.EDIT_PASTE,
                Command(id = ActionIds.EDIT_PASTE, title = "Paste", execute = { println("Paste stub") }, enabled = editEnabled)
            )
            reg.register(
                ActionIds.EDIT_SELECT_ALL,
                Command(id = ActionIds.EDIT_SELECT_ALL, title = "Select All", execute = { println("SelectAll stub") }, enabled = editEnabled)
            )

            // EDIT_MACROS for exact port (stub; menu wiring deferred with macros)
            reg.register(
                ActionIds.EDIT_MACROS,
                Command(id = ActionIds.EDIT_MACROS, title = "Macros", execute = { println("Macros stub (deferred)") })
            )

            // Find stub
            reg.register(
                ActionIds.EDIT_FIND,
                Command(id = ActionIds.EDIT_FIND, title = "Find", execute = { println("Find stub") }, enabled = isOpen)
            )

            // Find variant stubs (per ActionIds for compatibility)
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
        title = "RecipeJar"
    ) {
        MenuBar {
            // Note: Compose desktop Menu() supports mnemonic; Item() uses title+shortcut primarily (no direct MNEMONIC_KEY param like Swing; platform/ a11y handles). Commands retain mnemonic for future/compat.
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

                // print stub registered (see registration); omitted from menu for PR5 smallest scope

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
            files = files.value,
            currentRecipe = currentRecipe.value,
            isRecipeOpen = isRecipeOpen.value,
            isEditing = isEditing.value,
            onOpenRepo = ::pickDirectory,
            onSelectRecipe = onSelectRecipe
        )
    }
}
