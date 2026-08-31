package recipejar

import java.io.File
import java.util.prefs.Preferences

/**
 * Lightweight desktop preferences: last repository path, optional author,
 * last recipe **per repository** (scoped by absolute path).
 *
 * Backed by [java.util.prefs.Preferences] under node `recipejar`.
 */
object AppPrefs {
    private val prefs: Preferences = Preferences.userRoot().node("recipejar")

    private const val KEY_LAST_REPO = "lastRepoPath"
    private const val KEY_AUTHOR = "authorName"
    /** Prefixed keys: `lastRecipe::<absoluteRepoPath>` → filename. */
    private const val KEY_LAST_RECIPE_PREFIX = "lastRecipe::"
    /** Legacy global key from first PR-8 revision; migrated then removed. */
    private const val KEY_LAST_RECIPE_LEGACY = "lastRecipeFilename"
    /** Force single-pane phone-style layout on desktop (mobile UX testing). */
    private const val KEY_FORCE_COMPACT = "forceCompactLayout"
    /** Absolute path to user welcome HTML (blank → bundled classpath welcome). */
    private const val KEY_WELCOME_FILE = "welcomeFilePath"
    /** Appearance scheme id (forest/ocean/slate/warm/rose). */
    private const val KEY_APPEARANCE = "appearanceId"
    private const val KEY_APPEARANCE_DARK = "appearanceDark"

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

    /**
     * When true, shell uses compact (single-pane) layout regardless of window width.
     * For testing mobile-style navigation on desktop.
     */
    var forceCompactLayout: Boolean
        get() = prefs.getBoolean(KEY_FORCE_COMPACT, false)
        set(value) {
            prefs.putBoolean(KEY_FORCE_COMPACT, value)
            flushQuietly()
        }

    /**
     * Path to the welcome HTML file shown when no recipe is selected.
     * Blank means use the bundled default welcome.html.
     */
    var welcomeFilePath: String
        get() = prefs.get(KEY_WELCOME_FILE, "") ?: ""
        set(value) {
            val t = value.trim()
            if (t.isEmpty()) {
                prefs.remove(KEY_WELCOME_FILE)
            } else {
                prefs.put(KEY_WELCOME_FILE, t)
            }
            flushQuietly()
        }

    /**
     * Color scheme id applied at the Material theme wrapper.
     * Default is Forest (green), not Material3 pink/purple.
     */
    var appearanceId: String
        get() = prefs.get(KEY_APPEARANCE, AppearanceTheme.DEFAULT_ID)
            ?.takeIf { it.isNotBlank() }
            ?: AppearanceTheme.DEFAULT_ID
        set(value) {
            prefs.put(KEY_APPEARANCE, AppearanceTheme.parse(value).id)
            flushQuietly()
        }

    var appearanceDark: Boolean
        get() = prefs.getBoolean(KEY_APPEARANCE_DARK, false)
        set(value) {
            prefs.putBoolean(KEY_APPEARANCE_DARK, value)
            flushQuietly()
        }

    /**
     * Normalize [path] to an absolute directory path, or null if not a directory.
     * Always use this before persisting or comparing repo paths (avoids CWD-relative drift).
     */
    fun normalizeRepoPath(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val dir = File(path)
        if (!dir.isDirectory) return null
        return try {
            dir.absoluteFile.normalize().absolutePath
        } catch (_: Exception) {
            dir.absolutePath
        }
    }

    /** True if [path] exists and is a directory. */
    fun isValidRepoPath(path: String?): Boolean = normalizeRepoPath(path) != null

    /**
     * Last recipe filename for [repoPath] (must be a real directory for a stable key).
     * Returns null if unset or blank.
     */
    fun lastRecipeFor(repoPath: String?): String? {
        val key = recipeKey(repoPath) ?: return null
        migrateLegacyIfNeeded(key, repoPath!!)
        return prefs.get(key, null)?.takeIf { it.isNotBlank() }
    }

    /**
     * Remember [filename] as last recipe for [repoPath], or clear when [filename] is null/blank.
     * No-op if [repoPath] cannot be normalized to a directory.
     */
    fun setLastRecipe(repoPath: String?, filename: String?) {
        val key = recipeKey(repoPath) ?: return
        if (filename.isNullOrBlank()) {
            prefs.remove(key)
        } else {
            prefs.put(key, filename)
        }
        // Drop legacy global key so it cannot shadow scoped restore.
        if (prefs.get(KEY_LAST_RECIPE_LEGACY, null) != null) {
            prefs.remove(KEY_LAST_RECIPE_LEGACY)
        }
        flushQuietly()
    }

    private fun recipeKey(repoPath: String?): String? {
        val abs = normalizeRepoPath(repoPath) ?: repoPath?.takeIf { it.isNotBlank() }?.let {
            // Repo may already be open with an absolute path even if temporarily missing;
            // still scope by the absolute string when it looks absolute.
            try {
                File(it).absoluteFile.normalize().absolutePath
            } catch (_: Exception) {
                it
            }
        } ?: return null
        return KEY_LAST_RECIPE_PREFIX + abs
    }

    /**
     * One-time: if scoped key empty but legacy global filename set, copy into this repo key
     * only when it is the current [lastRepoPath] (avoid applying Untitled.html to every repo).
     */
    private fun migrateLegacyIfNeeded(scopedKey: String, repoPath: String) {
        if (prefs.get(scopedKey, null)?.isNotBlank() == true) return
        val legacy = prefs.get(KEY_LAST_RECIPE_LEGACY, null)?.takeIf { it.isNotBlank() } ?: return
        val lastRepo = lastRepoPath
        val abs = normalizeRepoPath(repoPath) ?: return
        if (lastRepo != null && normalizeRepoPath(lastRepo) == abs) {
            prefs.put(scopedKey, legacy)
            prefs.remove(KEY_LAST_RECIPE_LEGACY)
            flushQuietly()
        }
    }

    private fun flushQuietly() {
        try {
            prefs.flush()
        } catch (e: Exception) {
            Debug.error("Failed to flush preferences", e)
        }
    }
}
