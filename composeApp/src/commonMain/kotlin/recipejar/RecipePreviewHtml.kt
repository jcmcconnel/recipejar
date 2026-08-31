package recipejar

import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer

/**
 * HTML for the in-app readonly pane: program-footer (category links) over core content.
 * On-disk resting files keep browser-footer via repository save.
 */
object RecipePreviewHtml {
    fun forReadonly(recipe: Recipe): String =
        RecipeSerializer.serialize(recipe, "program-footer")

    fun forReadonlyFromDiskHtml(diskHtml: String): String {
        val recipe = RecipeSerializer.parse(diskHtml)
        return forReadonly(recipe)
    }

    /** Labels shown as category chips under the reader (program-footer equivalent). */
    fun labelsFromHtml(html: String?): List<String> {
        if (html.isNullOrBlank()) return emptyList()
        return try {
            RecipeSerializer.parse(html).labels.map { it.trim() }.filter { it.isNotEmpty() }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
