package recipejar.sample

import recipejar.html.RecipeSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Drives the real [SampleRecipeJar] + [RecipeSerializer] path used by mobile prototypes.
 */
class SampleRecipeJarTest {

    @Test
    fun loadRecipes_parsesBundledHtmlWithNonEmptyTitles() {
        val loaded = SampleRecipeJar.loadRecipes()
        assertTrue(loaded.size >= 2, "expected at least two sample recipes, got ${loaded.size}")
        loaded.forEach { (filename, recipe) ->
            assertTrue(filename.endsWith(".html"), filename)
            assertTrue(recipe.title.isNotBlank(), "blank title for $filename")
            assertTrue(
                recipe.ingredients.isNotEmpty() || recipe.procedure.isNotBlank() || recipe.notes.isNotBlank(),
                "expected body content for ${recipe.title}",
            )
        }
    }

    @Test
    fun bananaBread_roundtripsThroughShippedSerializer() {
        val html = SampleRecipeJar.htmlFor("BananaBread.html")
        assertNotNull(html)
        val recipe = RecipeSerializer.parse(html)
        assertEquals("Banana Bread", recipe.title.trim())
        assertTrue(recipe.ingredients.any { it.name.contains("banana", ignoreCase = true) })
        val again = RecipeSerializer.parse(RecipeSerializer.serialize(recipe, "browser-footer"))
        assertEquals(recipe.title, again.title)
    }

    @Test
    fun listFilenames_matchesLoadedEntries() {
        val names = SampleRecipeJar.listFilenames()
        assertTrue("BananaBread.html" in names)
        assertEquals(names, SampleRecipeJar.loadRecipes().map { it.first })
    }
}
