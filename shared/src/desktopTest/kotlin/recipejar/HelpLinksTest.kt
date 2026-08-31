package recipejar

import recipejar.html.RecipeSerializer
import recipejar.domain.Recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HelpLinksTest {

    @Test
    fun documentedUrl_matchesReferenceConfig() {
        assertEquals("https://github.com/jcmcconnel/recipejar", HelpLinks.WEB_URL)
        assertTrue(HelpLinks.isDocumentedHelpUrl(HelpLinks.WEB_URL))
        assertTrue(HelpLinks.isDocumentedHelpUrl(HelpLinks.WEB_URL + "/"))
        assertTrue(!HelpLinks.isDocumentedHelpUrl("http://code.google.com/p/recipejar/"))
    }

    @Test
    fun serializeFooters_pointAtGithubNotGoogleCode() {
        val html = RecipeSerializer.serialize(Recipe(title = "Help Probe"), "browser-footer")
        assertTrue(html.contains(HelpLinks.WEB_URL), html)
        assertTrue(!html.contains("code.google.com/p/recipejar"), html)
        val export = RecipeSerializer.serialize(Recipe(title = "Help Probe"), "export-footer")
        assertTrue(export.contains(HelpLinks.WEB_URL), export)
    }
}
