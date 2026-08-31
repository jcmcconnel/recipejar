package recipejar

import android.content.Context
import java.io.File

/**
 * Android app-private library. Host must call [installAndroidContext] once at startup.
 */
private var appContext: Context? = null

fun installAndroidContext(context: Context) {
    appContext = context.applicationContext
}

internal fun androidAppContext(): Context? = appContext

actual fun recipeLibraryRootPath(): String {
    val ctx = appContext
        ?: throw IllegalStateException("installAndroidContext() before recipeLibraryRootPath()")
    val dir = File(ctx.filesDir, "RecipeJar")
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath
}
