package recipejar.macro

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Macro manager persistence: list, add, save, reload via shipped [MacroStore].
 */
class MacroManagerPersistTest {

    @Test
    fun listAddSaveReload_userMacro() {
        val dir = Files.createTempDirectory("rj-macros-mgr").toFile()
        try {
            val seeded = MacroStore.load(dir.absolutePath)
            assertTrue(seeded.macros.isNotEmpty(), "defaults when repo has no macros.json")
            val added = MacroDefinition(
                name = "Degree",
                text = "&deg;F",
                mnemonic = "D",
            )
            val next = seeded.macros + added
            MacroStore.save(dir.absolutePath, next)
            val reloaded = MacroStore.load(dir.absolutePath)
            assertTrue(reloaded.macros.any { it.name == "Degree" && it.text == "&deg;F" })
            assertEquals(next.size, reloaded.macros.size)
        } finally {
            dir.deleteRecursively()
        }
    }
}
