package recipejar.macro

import java.io.File

/**
 * Result of loading macros for a repository (includes optional load note for UI).
 */
data class MacroLoadResult(
    val macros: List<MacroDefinition>,
    val note: String? = null,
)

/**
 * Desktop FS helpers: load/save macros.json under a recipe repository directory,
 * with legacy macros.txt import and default seeding.
 */
object MacroStore {

    /**
     * Load macros for [repoDir]:
     * 1. macros.json if present and valid
     * 2. else macros.txt if present (including when JSON is corrupt)
     * 3. else [MacroIo.DEFAULT_MACROS]
     *
     * Corrupt/unreadable JSON falls through rather than returning an empty list.
     */
    fun load(repoDir: String): MacroLoadResult {
        val base = File(repoDir)
        val jsonFile = File(base, MacroIo.JSON_FILENAME)
        var jsonFailed = false
        if (jsonFile.isFile) {
            try {
                val text = jsonFile.readText(Charsets.UTF_8)
                if (text.isBlank()) {
                    // Empty file is not a valid macros document — fall through
                    jsonFailed = true
                } else {
                    val macros = MacroIo.fromJson(text)
                    return MacroLoadResult(macros)
                }
            } catch (_: Exception) {
                jsonFailed = true
                // fall through to txt / defaults
            }
        }
        val txtFile = File(base, MacroIo.TXT_FILENAME)
        if (txtFile.isFile) {
            try {
                val macros = MacroIo.parseMacrosTxt(txtFile.readText(Charsets.UTF_8))
                val note = if (jsonFailed) {
                    "${MacroIo.JSON_FILENAME} invalid; loaded ${MacroIo.TXT_FILENAME}"
                } else {
                    null
                }
                return MacroLoadResult(macros, note)
            } catch (_: Exception) {
                // fall through to defaults
            }
        }
        val note = when {
            jsonFailed -> "${MacroIo.JSON_FILENAME} invalid; using defaults"
            else -> null
        }
        return MacroLoadResult(MacroIo.DEFAULT_MACROS, note)
    }

    fun save(repoDir: String, macros: List<MacroDefinition>) {
        val base = File(repoDir)
        if (!base.exists()) base.mkdirs()
        File(base, MacroIo.JSON_FILENAME).writeText(MacroIo.toJson(macros), Charsets.UTF_8)
    }

    /**
     * Import macros from a macros.txt path (or any text file in legacy format).
     * @throws IllegalArgumentException if the path is missing or not a file
     * @throws Exception on read failure
     */
    fun importTxt(path: String): List<MacroDefinition> {
        val f = File(path)
        if (!f.isFile) throw IllegalArgumentException("Import file not found: $path")
        return MacroIo.parseMacrosTxt(f.readText(Charsets.UTF_8))
    }
}
