package recipejar.persistence

import okio.FileSystem
import okio.Path.Companion.toPath
import recipejar.domain.Recipe
import recipejar.recipe.Ingredient
import recipejar.sample.SampleRecipeJar
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Multiplatform repository algorithm (Okio) used by iOS fixed library.
 */
class OkioRecipeRepositoryTest {

    @Test
    fun saveListLoad_and_seedIfEmpty() {
        val dir = Files.createTempDirectory("rj-okio").toFile()
        try {
            val repo = OkioRecipeRepository(dir.absolutePath)
            assertTrue(repo.isEmptyLibrary())
            repo.seedIfEmpty(SampleRecipeJar.entries.map { it.filename to it.html })
            assertFalse(repo.isEmptyLibrary())
            assertTrue(repo.listRecipes().isNotEmpty())
            // Second seed does not wipe
            val before = repo.listRecipes().size
            repo.seedIfEmpty(SampleRecipeJar.entries.map { it.filename to it.html })
            assertEquals(before, repo.listRecipes().size)

            val name = repo.listRecipes().first()
            val loaded = repo.loadRecipe(name)
            assertTrue(loaded.title.isNotBlank())

            val custom = Recipe(
                title = "Okio Test Cake",
                notes = "n",
                procedure = "bake",
                ingredients = mutableListOf(Ingredient("1", "cup", "flour")),
                labels = mutableListOf("Dessert"),
            )
            repo.saveRecipe(custom)
            assertTrue(repo.listRecipes().any { it.contains("OkioTestCake") || it.contains("Cake") })
            val html = repo.exportRecipeHtml(repo.filenameFor(custom))
            assertTrue(html.contains("export-footer") || html.contains("id=\"export-footer\""))
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun importHtmlBytes_roundTrip() {
        val dir = Files.createTempDirectory("rj-okio-imp").toFile()
        try {
            val repo = OkioRecipeRepository(dir.absolutePath)
            val sample = SampleRecipeJar.entries.first()
            val used = repo.importHtmlBytes(sample.html, sample.filename)
            assertTrue(used.endsWith(".html"))
            assertEquals(sample.html, repo.loadRecipeHtml(used))
        } finally {
            dir.deleteRecursively()
        }
    }
}
