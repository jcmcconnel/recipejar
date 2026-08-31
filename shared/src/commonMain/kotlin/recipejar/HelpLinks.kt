package recipejar

/**
 * Documented project Help URL (classic `HELP_URL` in `src/config.ini`).
 * Not the git remote — origin may be a LAN path.
 */
object HelpLinks {
    const val WEB_URL = "https://github.com/jcmcconnel/recipejar"

    /** True when [url] is the documented Help target (trailing slash optional). */
    fun isDocumentedHelpUrl(url: String): Boolean {
        val trimmed = url.trim().trimEnd('/')
        return trimmed.equals(WEB_URL.trimEnd('/'), ignoreCase = true)
    }
}
