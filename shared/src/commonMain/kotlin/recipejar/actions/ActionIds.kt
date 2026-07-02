package recipejar.actions

/**
 * Port of ActionIds.java
 * String IDs kept for compatibility.
 * No macros yet (PR5).
 */
object ActionIds {
    // File / Recipe actions
    const val FILE_SAVE = "file.save"
    const val FILE_DELETE = "file.delete"
    const val FILE_RENAME = "file.rename"
    const val FILE_EXPORT = "file.export"
    const val FILE_IMPORT = "file.import"
    const val FILE_TOGGLE_EDIT = "file.toggleEdit"
    const val FILE_NEW = "file.new"
    const val FILE_PRINT = "file.print"
    const val FILE_EXIT = "file.exit"

    // Edit actions (stubs for now; macros deferred)
    const val EDIT_CUT = "edit.cut"
    const val EDIT_COPY = "edit.copy"
    const val EDIT_PASTE = "edit.paste"
    const val EDIT_SELECT_ALL = "edit.selectAll"
    const val EDIT_MACROS = "edit.macros"
    const val EDIT_FIND = "edit.find"

    // Find stubs
    const val FIND_ALL = "find.all"
    const val FIND_TITLES = "find.titles"
    const val FIND_LABELS = "find.labels"
    const val FIND_NOTES = "find.notes"
    const val FIND_INGREDIENTS = "find.ingredients"
    const val FIND_PROCEDURES = "find.procedures"

    // Other ids for compatibility (not fully wired in PR5 menus)
    const val TOOLS_CONVERTER = "tools.converter"
    const val TOOLS_PREFERENCES = "tools.preferences"
    const val HELP_WEB = "help.web"
    const val HELP_ABOUT = "help.about"
}
