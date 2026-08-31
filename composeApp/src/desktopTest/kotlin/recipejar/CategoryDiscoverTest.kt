package recipejar

import recipejar.html.CategoryNavigation
import recipejar.sample.SampleRecipeJar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Index-level category discovery without selecting a recipe.
 * Drives [CategoryIndexLogic] — same rules as readonly chip → alphatab filter.
 */
class CategoryDiscoverTest {

    private val catalog: List<RecipeListItem> =
        SampleRecipeJar.loadRecipes().map { (fn, r) ->
            RecipeListItem(fn, r.title, r.labels.toList())
        }

    @Test
    fun allCategories_listsLabelsWithoutOpeningRecipe() {
        val cats = CategoryIndexLogic.allCategories(catalog)
        assertTrue(cats.isNotEmpty(), "sample jar has labels")
        assertTrue(cats.any { it.equals("Breakfast", ignoreCase = true) }, cats.toString())
        assertTrue(cats.any { it.equals("Bread", ignoreCase = true) }, cats.toString())
    }

    @Test
    fun categoriesForLetter_matchesCategoryNameBucket() {
        val bCats = CategoryIndexLogic.categoriesForLetter(catalog, 'B')
        assertTrue(bCats.any { it.equals("Breakfast", ignoreCase = true) }, bCats.toString())
        assertTrue(bCats.any { it.equals("Bread", ignoreCase = true) }, bCats.toString())
        // Not under A
        val aCats = CategoryIndexLogic.categoriesForLetter(catalog, 'A')
        assertTrue(aCats.none { it.equals("Breakfast", ignoreCase = true) })
    }

    @Test
    fun activate_withoutSelection_filtersRecipesAndSetsTab() {
        val act = CategoryIndexLogic.activate(catalog, "Breakfast")
        assertNotNull(act)
        assertEquals("Breakfast", act!!.categoryFilter)
        assertEquals(CategoryNavigation.tabIndexForCategory("Breakfast"), act.tabIndex)
        assertTrue(act.matchingRecipes.isNotEmpty())
        assertTrue(
            act.matchingRecipes.all { item ->
                item.labels.any { it.equals("Breakfast", ignoreCase = true) }
            },
        )
        // No need for a selectedFilename — listing is pure data.
        assertTrue(act.matchingRecipes.any { it.title.contains("Pancake", ignoreCase = true) ||
            it.filename.contains("Pancake", ignoreCase = true) ||
            it.filename.contains("French", ignoreCase = true) })
    }

    @Test
    fun activate_matchesReadonlyChipRules() {
        val fromIndex = CategoryIndexLogic.activate(catalog, "Bread")!!
        val fromNav = CategoryNavigation.tabIndexForCategory("Bread")
        assertEquals(fromNav, fromIndex.tabIndex)
        val filtered = CategoryIndexLogic.recipesForCategory(catalog, "Bread")
        assertEquals(fromIndex.matchingRecipes.map { it.filename }, filtered.map { it.filename })
    }
}
