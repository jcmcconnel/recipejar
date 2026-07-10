package recipejar

import java.io.File
import java.util.prefs.Preferences

/**
 * Lightweight desktop preferences: last repository path, optional author, last recipe.
 *
 * Backed by [java.util.prefs.Preferences] under node `recipejar`.
 */
object AppPrefs {
    private val prefs: Preferences = Preferences.userRoot().node("recipejar")

    private const val KEY_LAST_REPO = "lastRepoPath"
    private const val KEY_AUTHOR = "authorName"
    private const val KEY_LAST_RECIPE = "lastRecipeFilename"

    var lastRepoPath: String?
        get() = prefs.get(KEY_LAST_REPO, null)?.takeIf { it.isNotBlank() }
        set(value) {
            if (value.isNullOrBlank()) {
                prefs.remove(KEY_LAST_REPO)
            } else {
                prefs.put(KEY_LAST_REPO, value)
            }
            flushQuietly()
        }

    var authorName: String
        get() = prefs.get(KEY_AUTHOR, "") ?: ""
        set(value) {
            prefs.put(KEY_AUTHOR, value.trim())
            flushQuietly()
        }

    var lastRecipeFilename: String?
        get() = prefs.get(KEY_LAST_RECIPE, null)?.takeIf { it.isNotBlank() }
        set(value) {
            if (value.isNullOrBlank()) {
                prefs.remove(KEY_LAST_RECIPE)
            } else {
                prefs.put(KEY_LAST_RECIPE, value)
            }
            flushQuietly()
        }

    /** True if [path] exists and is a directory. */
    fun isValidRepoPath(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return File(path).isDirectory
    }

    private fun flushQuietly() {
        try {
            prefs.flush()
        } catch (e: Exception) {
            Debug.error("Failed to flush preferences", e)
        }
    }
}
