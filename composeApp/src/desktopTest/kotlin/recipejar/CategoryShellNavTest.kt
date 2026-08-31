package recipejar

import recipejar.html.CategoryNavigation
import recipejar.html.RecipeSerializer
import recipejar.sample.SampleRecipeJar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Shell-level category → alphatab navigation using the same rules as [App]
 * (program-footer activation → [CategoryNavigation.tabIndexForCategory] + label filter).
 */
class CategoryShellNavTest {

    @Test
    fun programFooterCategory_movesAlphaTabToCategoryLetter() {
        val html = SampleRecipeJar.htmlFor("Pancakes.html")!!
        val recipe = RecipeSerializer.parse(html)
        assertTrue(recipe.labels.any { it.equals("Breakfast", ignoreCase = true) })

        val program = RecipePreviewHtml.forReadonly(recipe)
        assertTrue(program.contains("program-footer"))
        assertTrue(program.contains(RecipeSerializer.CATEGORY_LINK_SCHEME))

        val href = RecipeSerializer.CATEGORY_LINK_SCHEME +
            CategoryNavigation.encodeLabel("Breakfast")
        val tab = CategoryNavigation.tabIndexFromCategoryActivation(href)
        assertEquals(CategoryNavigation.tabIndexForCategory("Breakfast"), tab)
        assertEquals('B' - 'A', tab)
    }

    @Test
    fun categoryFilter_listsRecipesWithLabel_notJustTitleLetter() {
        val items = SampleRecipeJar.loadRecipes().map { (fn, r) ->
            RecipeListItem(fn, r.title, r.labels.toList())
        }
        val cat = "Breakfast"
        val filtered = items.filter { item ->
            item.labels.any { it.equals(cat, ignoreCase = true) }
        }
        assertTrue(filtered.isNotEmpty(), "sample jar should have Breakfast recipes")
        assertTrue(filtered.any { it.filename.contains("Pancake", ignoreCase = true) ||
            it.title.contains("Pancake", ignoreCase = true) ||
            it.filename.contains("French", ignoreCase = true) })
        // Tab for category letter matches shell mapping
        assertEquals(
            CategoryNavigation.tabIndexForCategory(cat),
            'B' - 'A',
        )
    }

    @Test
    fun letterBucket_matchesAppHelper() {
        assertEquals(letterBucket("Breakfast ideas"), CategoryNavigation.letterBucket("Breakfast ideas"))
        assertEquals(letterBucket("123"), CategoryNavigation.letterBucket("123"))
    }
}
