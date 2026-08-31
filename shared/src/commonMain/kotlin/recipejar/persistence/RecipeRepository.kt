package recipejar.persistence

import recipejar.domain.Recipe

/**
 * Repository abstraction for listing, load, save recipes + index maintenance.
 * Desktop: [recipejar.persistence.FileSystemRecipeRepository].
 * Adapted from original Java RecipeRepository + IndexFile orchestration to domain.Recipe + PR2 serializer.
 */
interface RecipeRepository {
    /** Absolute path of the open repository directory. */
    val location: String

    /** Basenames of recipe `.html` files (excludes `index.html` and mac `._*` junk). */
    fun listRecipes(): List<String>

    /** Load by on-disk filename key. */
    fun loadRecipe(filename: String): Recipe

    /**
     * Create or update a recipe.
     * If [originalFilename] is set and differs from sanitized(title)+".html", renames (atomic when possible).
     * Always rebuilds `index.html` after a successful write.
     */
    fun saveRecipe(recipe: Recipe, originalFilename: String? = null)

    fun deleteRecipe(filename: String)

    /**
     * Import an external `.html` file losslessly (byte copy).
     * Target name is derived from the parsed title. Returns the used filename.
     */
    fun importRecipe(sourcePath: String): String

    /** Export using the export-footer serializer variant. */
    fun exportRecipe(filename: String, targetPath: String)

    /**
     * Export the entire open recipe directory as a zip of the on-disk tree
     * (HTML and supporting assets as present). Optional for hosts without FS zip support.
     */
    fun exportDirectoryZip(targetZipPath: String) {
        throw UnsupportedOperationException("Directory zip export is not available on this host")
    }
}
