package recipejar

import recipejar.html.CategoryNavigation

/**
 * Pure index-level category discovery — same activation rules as readonly chips
 * ([CategoryNavigation] + label-filtered recipe list).
 *
 * Categories are discoverable without opening a recipe: list labels for the current
 * letter (or all labels), then activate to filter the alphatab listing.
 */
object CategoryIndexLogic {

    /**
     * Distinct category/label names present on any recipe, sorted case-insensitively.
     */
    fun allCategories(recipes: List<RecipeListItem>): List<String> {
        val set = linkedSetOf<String>()
        for (item in recipes) {
            for (raw in item.labels) {
                val t = raw.trim()
                if (t.isNotEmpty()) set.add(t)
            }
        }
        return set.sortedBy { it.lowercase() }
    }

    /**
     * Categories whose name falls in [letter] bucket ('A'..'Z' or '0' for Other),
     * matching classic index placement (label letter, not recipe title letter).
     */
    fun categoriesForLetter(recipes: List<RecipeListItem>, letter: Char): List<String> {
        val bucket = if (letter in 'A'..'Z' || letter == '0') letter else '0'
        return allCategories(recipes).filter { CategoryNavigation.letterBucket(it) == bucket }
    }

    /**
     * Recipes carrying [category] (case-insensitive label match) — same filter as
     * shell [categoryFilter] after [activateCategory].
     */
    fun recipesForCategory(recipes: List<RecipeListItem>, category: String): List<RecipeListItem> {
        val cat = category.trim()
        if (cat.isEmpty()) return emptyList()
        return recipes
            .filter { item -> item.labels.any { it.equals(cat, ignoreCase = true) } }
            .sortedBy { titleSortKey(it.title) }
    }

    /**
     * Tab index to select when activating [category] (letter of category name).
     */
    fun tabIndexForCategory(category: String): Int =
        CategoryNavigation.tabIndexForCategory(category)

    /**
     * Result of activating a category from the index (no recipe need be selected).
     */
    data class Activation(
        val categoryFilter: String,
        val tabIndex: Int,
        val matchingRecipes: List<RecipeListItem>,
    )

    fun activate(recipes: List<RecipeListItem>, category: String): Activation? {
        val cat = category.trim()
        if (cat.isEmpty()) return null
        return Activation(
            categoryFilter = cat,
            tabIndex = tabIndexForCategory(cat),
            matchingRecipes = recipesForCategory(recipes, cat),
        )
    }
}
