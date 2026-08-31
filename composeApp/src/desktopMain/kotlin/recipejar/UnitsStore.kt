package recipejar

import recipejar.recipe.UnitDef
import recipejar.recipe.UnitsCatalog
import java.io.File

/**
 * Load/save the user-maintainable units catalog (plain-text units.txt).
 * Prefer [userUnitsFile]; fall back to bundled classpath resource.
 */
object UnitsStore {
    private const val RESOURCE_NAME = "units.txt"

    /** Stable user file under ~/.recipejar/units.txt */
    fun userUnitsFile(): File {
        val home = System.getProperty("user.home") ?: "."
        val dir = File(home, ".recipejar").also { it.mkdirs() }
        return File(dir, "units.txt")
    }

    fun load(): List<UnitDef> {
        val user = userUnitsFile()
        if (user.isFile) {
            return try {
                UnitsCatalog.parse(user.readText(Charsets.UTF_8))
            } catch (e: Exception) {
                Debug.error("Failed to read user units.txt", e)
                loadBundled()
            }
        }
        return loadBundled()
    }

    fun loadBundled(): List<UnitDef> {
        val text = loadClasspathText(RESOURCE_NAME)
        if (text.isBlank()) return emptyList()
        return UnitsCatalog.parse(text)
    }

    fun save(units: List<UnitDef>) {
        val file = userUnitsFile()
        file.parentFile?.mkdirs()
        file.writeText(UnitsCatalog.serialize(units), Charsets.UTF_8)
    }

    /** Display labels (plurals) for the ingredient picker. */
    fun dropdownPlurals(units: List<UnitDef> = load()): List<String> =
        units.map { it.displayName() }

    private fun loadClasspathText(resourceName: String): String {
        val stream = Thread.currentThread().contextClassLoader?.getResourceAsStream(resourceName)
            ?: object {}.javaClass.getResourceAsStream("/$resourceName")
            ?: object {}.javaClass.classLoader?.getResourceAsStream(resourceName)
        return try {
            stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        } catch (e: Exception) {
            Debug.error("Failed to load resource $resourceName", e)
            ""
        }
    }
}
