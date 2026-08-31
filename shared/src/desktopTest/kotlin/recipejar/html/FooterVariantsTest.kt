package recipejar.html

import recipejar.domain.Recipe
import recipejar.recipe.Ingredient
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

/**
 * Drives shipped [RecipeSerializer] footer variants:
 * browser (resting/index nav), program (category scheme links), export (plain labels).
 */
class FooterVariantsTest {

    private fun sampleRecipe(): Recipe = Recipe(
        title = "Footer Probe",
        notes = "n",
        procedure = "p",
        ingredients = mutableListOf(Ingredient("1", "cup", "flour")),
        labels = mutableListOf("Breakfast", "Quick Meal"),
    )

    @Test
    fun browserFooter_includesIndexNavigationAndCategoryAnchors() {
        val html = RecipeSerializer.serialize(sampleRecipe(), "browser-footer")
        assertTrue(html.contains("id=\"browser-footer\"") || html.contains("id='browser-footer'"))
        assertTrue(html.contains("href=\"index.html\""), "resting file must link to index")
        assertTrue(html.contains("Index"), "index link label")
        assertTrue(html.contains("index.html#Breakfast") || html.contains("index.html#Quick_Meal"))
        assertFalse(html.contains(RecipeSerializer.CATEGORY_LINK_SCHEME), "browser uses website anchors")
        // round-trip core
        val re = RecipeSerializer.parse(html)
        assertEquals("Footer Probe", re.title)
        assertEquals(listOf("Breakfast", "Quick Meal"), re.labels)
    }

    @Test
    fun programFooter_emitsCategorySchemeLinks_notIndexChrome() {
        val html = RecipeSerializer.serialize(sampleRecipe(), "program-footer")
        assertTrue(html.contains("id=\"program-footer\"") || html.contains("id='program-footer'"))
        assertTrue(html.contains(RecipeSerializer.CATEGORY_LINK_SCHEME), "program uses in-app category links")
        assertTrue(html.contains("Breakfast"))
        assertTrue(html.contains("Quick"))
        // No browser index chrome inside program footer body
        assertFalse(
            html.contains("<a href=\"index.html\">Index</a>"),
            "program footer must not include resting Index link",
        )
        // Category links are not plain index.html# only
        val breakfastHref = CategoryNavigation.encodeLabel("Breakfast")
        assertTrue(html.contains("$breakfastHref") || html.contains("Breakfast"))
        assertFalse(
            Regex("""id="program-footer"[\s\S]*href="index\.html#""").containsMatchIn(html) &&
                !html.contains(RecipeSerializer.CATEGORY_LINK_SCHEME),
            "program footer should prefer scheme links",
        )
        val re = RecipeSerializer.parse(html)
        assertEquals("Footer Probe", re.title)
        assertEquals(2, re.labels.size)
    }

    @Test
    fun exportFooter_listsCategoriesWithoutHrefCategoryLinks() {
        val html = RecipeSerializer.serialize(sampleRecipe(), "export-footer")
        assertTrue(html.contains("id=\"export-footer\"") || html.contains("id='export-footer'"))
        assertTrue(html.contains("Breakfast"))
        assertTrue(html.contains("Quick Meal"))
        // No category navigation links
        assertFalse(html.contains("index.html#Breakfast"))
        assertFalse(html.contains("index.html#Quick_Meal"))
        assertFalse(html.contains(RecipeSerializer.CATEGORY_LINK_SCHEME))
        assertFalse(html.contains("<a href=\"index.html\">Index</a>"))
        // Labels appear as plain text (no anchor wrapping Breakfast)
        assertFalse(
            Regex("""<a[^>]*>\s*Breakfast\s*</a>""").containsMatchIn(html),
            "export must not wrap categories in links",
        )
        val re = RecipeSerializer.parse(html)
        assertEquals(listOf("Breakfast", "Quick Meal"), re.labels)
    }

    @Test
    fun threeFooters_areDistinctAndRoundTripCoreFields() {
        val r = sampleRecipe()
        val browser = RecipeSerializer.serialize(r, "browser-footer")
        val program = RecipeSerializer.serialize(r, "program-footer")
        val export = RecipeSerializer.serialize(r, "export-footer")
        assertTrue(browser.contains("browser-footer"))
        assertTrue(program.contains("program-footer"))
        assertTrue(export.contains("export-footer"))
        assertTrue(browser != program)
        assertTrue(program != export)
        assertTrue(browser != export)
        for (html in listOf(browser, program, export)) {
            val p = RecipeSerializer.parse(html)
            assertEquals(r.title, p.title)
            assertEquals(r.labels, p.labels)
            assertEquals(r.ingredients.size, p.ingredients.size)
        }
    }
}
