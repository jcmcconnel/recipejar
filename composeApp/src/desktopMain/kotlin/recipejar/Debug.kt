package recipejar

/**
 * Thin logging helper — replaces bare [println] for app diagnostics.
 * Prefixes messages so desktop console output is easy to filter.
 */
object Debug {
    private const val TAG = "RecipeJar"

    fun log(message: String) {
        println("[$TAG] $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        System.err.println("[$TAG] ERROR: $message")
        throwable?.printStackTrace()
    }
}
