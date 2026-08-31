package recipejar

import java.io.File

/**
 * Desktop keeps picker-based open; this path is only for shared API / tests.
 */
actual fun recipeLibraryRootPath(): String {
    val home = System.getProperty("user.home") ?: "."
    val dir = File(home, ".recipejar/library")
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath
}
