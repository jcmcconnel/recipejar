package recipejar

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * Fixed iOS library: Library/Application Support/RecipeJar
 */
@OptIn(ExperimentalForeignApi::class)
actual fun recipeLibraryRootPath(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(
        NSApplicationSupportDirectory,
        NSUserDomainMask,
    )
    val first = urls.firstOrNull() as? NSURL
        ?: error("Application Support directory unavailable")
    val base = first.path ?: error("Application Support path unavailable")
    val root = "$base/RecipeJar"
    NSFileManager.defaultManager.createDirectoryAtPath(
        root,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return root
}
