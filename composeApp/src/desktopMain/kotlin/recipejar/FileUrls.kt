package recipejar

import java.io.File
import java.net.URI

/**
 * CEF / KCEF is picky about file URLs. Java [File.toURI] often emits `file:/path`
 * (one slash after the scheme); Chromium expects `file:///path` for absolute local files.
 */
object FileUrls {
    /** Absolute `file:///…` URL for an existing or target local file. */
    fun fromFile(file: File): String {
        val abs = try {
            file.absoluteFile.normalize()
        } catch (_: Exception) {
            file.absoluteFile
        }
        val uri = abs.toURI()
        return normalizeFileUri(uri.toString())
    }

    /**
     * Force `file:/abs` → `file:///abs` so CEF can resolve local recipes.
     * Leaves non-file schemes unchanged.
     */
    fun normalizeFileUri(url: String): String {
        if (url.isBlank()) return url
        // Already correct: file:///…
        if (url.startsWith("file:///")) return url
        // Java-style absolute: file:/Users/… or file:/C:/…
        if (url.startsWith("file:/") && !url.startsWith("file://")) {
            return "file://" + url.removePrefix("file:")
        }
        // file://hostname/path — leave alone if host present
        return url
    }

    /**
     * Directory base for relative CSS/images: `file:///repo/` (trailing slash).
     */
    fun directoryBaseUrl(dir: File): String {
        val base = fromFile(dir)
        return if (base.endsWith("/")) base else "$base/"
    }

    /** Parse a file URL back to a [File], or null if not a local file URL. */
    fun toFileOrNull(url: String): File? {
        if (url.isBlank() || !url.startsWith("file:")) return null
        return try {
            File(URI(normalizeFileUri(url)))
        } catch (_: Exception) {
            null
        }
    }
}
