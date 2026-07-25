package recipejar

/**
 * Marker so the Android source set is non-empty and linked into the library AAR.
 * Domain code lives in commonMain; filesystem adapters follow in Phase 1A.
 */
object AndroidSharedMarker {
    const val TARGET: String = "android"
}
