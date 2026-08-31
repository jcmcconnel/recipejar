package recipejar

import recipejar.html.RecipeSerializer
import recipejar.sample.SampleRecipeJar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipePlainTextTest {

    @Test
    fun recipePlainTextFromHtml_showsTitleAndSections_notRawTags() {
        val html = SampleRecipeJar.htmlFor("BananaBread.html")!!
        val text = recipePlainTextFromHtml(html)
        assertTrue(text.contains("Banana Bread"), text)
        assertTrue(text.contains("Notes:") || text.contains("How to Cook"), text)
        assertTrue(text.contains("flour") || text.contains("You will need"), text.lowercase())
        assertFalse(text.contains("<div"), text)
        assertFalse(text.contains("<span"), text)
    }

    @Test
    fun titleFromRecipeHtml_prefersTitleTagOverFilename() {
        val html = SampleRecipeJar.htmlFor("BananaBread.html")!!
        assertTrue(titleFromRecipeHtml(html).contains("Banana Bread"))
        assertTrue(titleFromRecipeHtml(null).isEmpty())
    }

    @Test
    fun formatIngredientLine_and_parseDriveReadonlyDocument() {
        val html = SampleRecipeJar.htmlFor("Pancakes.html")!!
        val recipe = RecipeSerializer.parse(html)
        assertEquals("Pancakes", recipe.title)
        assertTrue(recipe.ingredients.isNotEmpty())
        val line = formatIngredientLine(recipe.ingredients.first())
        assertTrue(line.startsWith("• "), line)
        // No raw HTML in formatted lines
        assertFalse(line.contains("<"), line)
    }
}
