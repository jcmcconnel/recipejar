package recipejar.html

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Shell-level category → alphatab mapping used when program-footer categories activate.
 */
class CategoryNavigationTest {

    @Test
    fun tabIndexForCategory_lettersAndOther() {
        assertEquals(0, CategoryNavigation.tabIndexForCategory("Apple cake"))
        assertEquals(1, CategoryNavigation.tabIndexForCategory("Breakfast"))
        assertEquals('B' - 'A', CategoryNavigation.tabIndexForCategory("bread"))
        assertEquals(25, CategoryNavigation.tabIndexForCategory("Zucchini"))
        assertEquals(26, CategoryNavigation.tabIndexForCategory("123 odd"))
        assertEquals(26, CategoryNavigation.tabIndexForCategory(""))
    }

    @Test
    fun labelFromHref_decodesProgramScheme() {
        val label = "Quick Meal"
        val href = RecipeSerializer.CATEGORY_LINK_SCHEME + CategoryNavigation.encodeLabel(label)
        assertEquals(label, CategoryNavigation.labelFromHref(href))
        assertNull(CategoryNavigation.labelFromHref("index.html#Breakfast"))
        assertNull(CategoryNavigation.labelFromHref(null))
    }

    @Test
    fun tabIndexFromCategoryActivation_hrefAndRawLabel() {
        val href = RecipeSerializer.CATEGORY_LINK_SCHEME + CategoryNavigation.encodeLabel("Breakfast")
        assertEquals(
            CategoryNavigation.tabIndexForCategory("Breakfast"),
            CategoryNavigation.tabIndexFromCategoryActivation(href),
        )
        assertEquals(
            CategoryNavigation.tabIndexForCategory("Desserts"),
            CategoryNavigation.tabIndexFromCategoryActivation("Desserts"),
        )
        assertNull(CategoryNavigation.tabIndexFromCategoryActivation("index.html#Breakfast"))
        assertNull(CategoryNavigation.tabIndexFromCategoryActivation(null))
    }

    @Test
    fun programFooter_linksRoundTripThroughNavigationHelper() {
        val recipe = recipejar.domain.Recipe(
            title = "Nav Probe",
            labels = mutableListOf("Breakfast", "Quick Meal"),
        )
        val html = RecipeSerializer.serialize(recipe, "program-footer")
        assertTrue(html.contains(RecipeSerializer.CATEGORY_LINK_SCHEME))
        val breakfastIdx = CategoryNavigation.tabIndexFromCategoryActivation(
            RecipeSerializer.CATEGORY_LINK_SCHEME + CategoryNavigation.encodeLabel("Breakfast"),
        )
        assertEquals('B' - 'A', breakfastIdx)
        val quickIdx = CategoryNavigation.tabIndexFromCategoryActivation(
            RecipeSerializer.CATEGORY_LINK_SCHEME + CategoryNavigation.encodeLabel("Quick Meal"),
        )
        assertEquals('Q' - 'A', quickIdx)
    }
}
