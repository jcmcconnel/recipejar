package recipejar.persistence

import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import recipejar.recipe.Ingredient
import recipejar.search.SearchCatalogEntry
import recipejar.search.SearchScope
import recipejar.search.filterRecipesByQuery
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * End-user recipe path: create, edit, reload, search, import, export.
 * Writes only to a temp directory (never Test/Recipes).
 */
class RecipeLifecycleTest {

    @Test
    fun createEditReloadSearch_andImportExportFooters() {
        val tmp = Files.createTempDirectory("rj-lifecycle").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)

            val created = Recipe(
                title = "Weekday Oats",
                notes = "family breakfast",
                procedure = "simmer oats",
                ingredients = mutableListOf(Ingredient("1", "Cups", "rolled oats")),
                labels = mutableListOf("Breakfast"),
            )
            repo.saveRecipe(created)
            val name = repo.filenameFor(created)
            assertTrue(File(tmp, name).isFile)

            val onDisk = File(tmp, name).readText(Charsets.UTF_8)
            assertTrue(onDisk.contains("browser-footer"), "resting save uses browser-footer")
            assertTrue(onDisk.contains("href=\"index.html\""), "browser-footer index nav")

            val loaded = repo.loadRecipe(name)
            assertEquals("Weekday Oats", loaded.title)
            assertEquals("family breakfast", loaded.notes.trim())
            assertEquals("simmer oats", loaded.procedure.trim())
            assertEquals("Cups", loaded.ingredients.single().unit)
            assertEquals(listOf("Breakfast"), loaded.labels)

            loaded.title = "Weekend Oats"
            loaded.notes = "weekend notes"
            loaded.procedure = "stir often"
            loaded.ingredients[0] = Ingredient("2", "Cups", "steel-cut oats")
            loaded.labels.clear()
            loaded.labels.add("Brunch")
            repo.saveRecipe(loaded, originalFilename = name)
            val renamed = repo.filenameFor(loaded)
            val reloaded = repo.loadRecipe(renamed)
            assertEquals("Weekend Oats", reloaded.title)
            assertEquals("weekend notes", reloaded.notes.trim())
            assertEquals("stir often", reloaded.procedure.trim())
            assertEquals("2", reloaded.ingredients.single().quantity)
            assertEquals("steel-cut oats", reloaded.ingredients.single().name)
            assertEquals(listOf("Brunch"), reloaded.labels)

            val second = Recipe(
                title = "Garlic Pasta",
                notes = "weeknight",
                procedure = "boil",
                ingredients = mutableListOf(Ingredient("8", "oz", "spaghetti")),
                labels = mutableListOf("Dinner"),
            )
            repo.saveRecipe(second)

            val catalog = repo.listRecipes().map { fn ->
                val r = repo.loadRecipe(fn)
                SearchCatalogEntry(fn, r.title)
            }
            val fields = repo.listRecipes().associateWith { fn ->
                val r = repo.loadRecipe(fn)
                mapOf(
                    SearchScope.LABELS to r.labels.joinToString(),
                    SearchScope.NOTES to r.notes,
                    SearchScope.INGREDIENTS to r.ingredients.joinToString(" ") {
                        "${it.quantity} ${it.unit} ${it.name}"
                    },
                    SearchScope.PROCEDURE to r.procedure,
                )
            }
            val byTitle = filterRecipesByQuery(catalog, "oats", setOf(SearchScope.TITLES), fields)
            assertEquals(listOf(renamed), byTitle.map { it.filename })
            val byLabel = filterRecipesByQuery(catalog, "Dinner", setOf(SearchScope.LABELS), fields)
            assertEquals(listOf(repo.filenameFor(second)), byLabel.map { it.filename })
            val byIng = filterRecipesByQuery(catalog, "spaghetti", setOf(SearchScope.INGREDIENTS), fields)
            assertEquals(1, byIng.size)

            // Import: write a standalone HTML then import into the repo
            val importSrc = File(tmp, "incoming.html")
            importSrc.writeText(
                RecipeSerializer.serialize(
                    Recipe(
                        title = "Imported Pie",
                        notes = "from aunt",
                        procedure = "bake",
                        labels = mutableListOf("Dessert"),
                    ),
                    "browser-footer",
                ),
                Charsets.UTF_8,
            )
            val importedName = repo.importRecipe(importSrc.absolutePath)
            val imported = repo.loadRecipe(importedName)
            assertEquals("Imported Pie", imported.title)
            assertEquals("from aunt", imported.notes.trim())

            val exportOut = File(tmp, "exported-pie.html")
            repo.exportRecipe(importedName, exportOut.absolutePath)
            val exportHtml = exportOut.readText(Charsets.UTF_8)
            assertTrue(exportHtml.contains("export-footer"))
            assertFalse(exportHtml.contains("recipejar://category/"))
            assertFalse(exportHtml.contains("<a href=\"index.html\">Index</a>"))

            val inApp = RecipeSerializer.serialize(imported, "program-footer")
            assertTrue(inApp.contains("program-footer"))
            assertTrue(inApp.contains(RecipeSerializer.CATEGORY_LINK_SCHEME))
            assertFalse(inApp.contains("<a href=\"index.html\">Index</a>"))

            val zipOut = File(tmp, "bundle.zip")
            repo.exportDirectoryZip(zipOut.absolutePath)
            assertTrue(zipOut.isFile && zipOut.length() > 0)
        } finally {
            tmp.deleteRecursively()
        }
    }
}
