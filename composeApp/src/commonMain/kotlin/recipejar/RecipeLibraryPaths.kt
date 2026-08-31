package recipejar

/**
 * Platform-specific fixed recipe library root (absolute path).
 * iOS: Library/Application Support/RecipeJar
 * Android: app filesDir/RecipeJar
 * Desktop: unused for open-picker workflow (returns a cache fallback for tests).
 */
expect fun recipeLibraryRootPath(): String
