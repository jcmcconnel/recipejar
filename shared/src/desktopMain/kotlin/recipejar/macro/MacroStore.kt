package recipejar.macro

import java.io.File

/**
 * Desktop FS helpers: load/save macros.json under a recipe repository directory,
 * with legacy macros.txt import and default seeding.
 */
object MacroStore {

    /**
     * Load macros for [repoDir]:
     * 1. macros.json if present
     * 2. else macros.txt if present (legacy import; does not auto-write JSON)
     * 3. else [MacroIo.DEFAULT_MACROS]
     */
    fun load(repoDir: String): List<MacroDefinition> {
        val base = File(repoDir)
        val jsonFile = File(base, MacroIo.JSON_FILENAME)
        if (jsonFile.isFile) {
            return try {
                MacroIo.fromJson(jsonFile.readText(Charsets.UTF_8))
            } catch (_: Exception) {
                emptyList()
            }
        }
        val txtFile = File(base, MacroIo.TXT_FILENAME)
        if (txtFile.isFile) {
            return try {
                MacroIo.parseMacrosTxt(txtFile.readText(Charsets.UTF_8))
            } catch (_: Exception) {
                emptyList()
            }
        }
        return MacroIo.DEFAULT_MACROS
    }

    fun save(repoDir: String, macros: List<MacroDefinition>) {
        val base = File(repoDir)
        if (!base.exists()) base.mkdirs()
        File(base, MacroIo.JSON_FILENAME).writeText(MacroIo.toJson(macros), Charsets.UTF_8)
    }

    /**
     * Import macros from a macros.txt path (or any text file in legacy format).
     */
    fun importTxt(path: String): List<MacroDefinition> {
        val f = File(path)
        if (!f.isFile) return emptyList()
        return MacroIo.parseMacrosTxt(f.readText(Charsets.UTF_8))
    }
}
