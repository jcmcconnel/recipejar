package recipejar.persistence

import recipejar.domain.Recipe
import recipejar.recipe.Ingredient
import java.io.File
import java.nio.file.Files
import kotlin.test.*

/**
 * PR-3 repository tests. Writes only to temp dirs; never mutates Test/Recipes goldens.
 */
class FileSystemRecipeRepositoryTest {

    private val corpusDir = File("../Test/Recipes").let { if (it.exists()) it else File("Test/Recipes") }

    @Test
    fun listRecipes_fromCorpus_excludesIndexAndMacJunk() {
        assumeCorpus()
        val repo = FileSystemRecipeRepository(corpusDir.absolutePath)
        val names = repo.listRecipes()
        assertTrue(names.isNotEmpty(), "expected recipes in corpus")
        assertFalse(names.any { it.equals("index.html", ignoreCase = true) })
        assertFalse(names.any { it.startsWith("._") })
        assertTrue(names.contains("BananaBread.html") || names.any { it.contains("Banana") })
    }

    @Test
    fun loadRecipe_parsesKnownCorpusFile() {
        assumeCorpus()
        val repo = FileSystemRecipeRepository(corpusDir.absolutePath)
        val name = repo.listRecipes().first { it.contains("Banana", ignoreCase = true) || it == "Test1.html" }
        val recipe = repo.loadRecipe(name)
        assertTrue(recipe.title.isNotBlank(), "title from $name")
    }

    @Test
    fun saveLoadRoundtrip_inTempDir() {
        val tmp = Files.createTempDirectory("rj-repo-save").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val recipe = Recipe(
                title = "Temp Pancakes",
                notes = "notes here",
                procedure = "mix and fry",
                ingredients = mutableListOf(Ingredient("1", "cup", "flour")),
                labels = mutableListOf("Breakfast", "Test Label")
            )
            repo.saveRecipe(recipe)

            val expectedName = repo.filenameFor(recipe)
            assertTrue(File(tmp, expectedName).exists(), "recipe file written")
            assertTrue(File(tmp, "index.html").exists(), "index rebuilt")

            val loaded = repo.loadRecipe(expectedName)
            assertEquals("Temp Pancakes", loaded.title)
            assertEquals(normalizeWs("notes here"), normalizeWs(loaded.notes))
            assertEquals(1, loaded.ingredients.size)
            assertEquals("flour", loaded.ingredients[0].name)
            assertTrue(loaded.labels.any { it.contains("Breakfast") })

            val index = File(tmp, "index.html").readText(Charsets.UTF_8)
            assertTrue(index.contains("id=\"letterT\""), "title letter section")
            assertTrue(index.contains("Temp Pancakes"))
            assertTrue(index.contains("id=\"Breakfast\"") || index.contains("Breakfast"), "label category")
            assertTrue(index.contains("id=\"letter0\""), "Other section present")
            assertTrue(index.contains("<h2>Other</h2>"), "Other heading")
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun renameOnTitleChange_removesOldFile() {
        val tmp = Files.createTempDirectory("rj-repo-rename").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val recipe = Recipe(title = "Old Name", notes = "n", procedure = "p")
            repo.saveRecipe(recipe)
            val oldName = repo.filenameFor(recipe)
            assertTrue(File(tmp, oldName).exists())

            recipe.title = "New Name"
            repo.saveRecipe(recipe, originalFilename = oldName)

            val newName = repo.filenameFor(recipe)
            assertEquals("NewName.html", newName)
            assertTrue(File(tmp, newName).exists(), "new file exists")
            assertFalse(File(tmp, oldName).exists(), "old file removed")
            assertEquals("New Name", repo.loadRecipe(newName).title)

            val index = File(tmp, "index.html").readText(Charsets.UTF_8)
            assertTrue(index.contains("New Name"))
            assertFalse(index.contains("Old Name"))
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun deleteRecipe_removesFileAndIndexEntry() {
        val tmp = Files.createTempDirectory("rj-repo-del").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val recipe = Recipe(title = "Doomed", notes = "", procedure = "")
            repo.saveRecipe(recipe)
            val name = repo.filenameFor(recipe)
            assertTrue(File(tmp, name).exists())

            repo.deleteRecipe(name)
            assertFalse(File(tmp, name).exists())
            val index = File(tmp, "index.html").readText(Charsets.UTF_8)
            assertFalse(index.contains("Doomed"))
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun importRecipe_losslessCopy() {
        assumeCorpus()
        val src = File(corpusDir, "Test1.html")
        if (!src.exists()) {
            // alternate small file
            val any = corpusDir.listFiles { f -> f.isFile && f.name.endsWith(".html") && f.name != "index.html" }
                ?.firstOrNull() ?: fail("no corpus html")
            runImport(any)
        } else {
            runImport(src)
        }
    }

    @Test
    fun exportRecipe_usesExportFooter() {
        val tmp = Files.createTempDirectory("rj-repo-export").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val recipe = Recipe(title = "Export Me", notes = "n", procedure = "p")
            repo.saveRecipe(recipe)
            val name = repo.filenameFor(recipe)
            val out = File(tmp, "exported.html")
            repo.exportRecipe(name, out.absolutePath)
            val html = out.readText(Charsets.UTF_8)
            assertTrue(
                html.contains("export-footer") || html.contains("id=\"export-footer\""),
                "export footer variant"
            )
        } finally {
            tmp.deleteRecursively()
        }
    }

    @Test
    fun blankTitle_saveThrows() {
        val tmp = Files.createTempDirectory("rj-repo-blank").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            assertFailsWith<IllegalArgumentException> {
                repo.saveRecipe(Recipe(title = "  ", notes = "", procedure = ""))
            }
        } finally {
            tmp.deleteRecursively()
        }
    }

    private fun runImport(src: File) {
        val tmp = Files.createTempDirectory("rj-repo-import").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val origBytes = src.readBytes()
            val used = repo.importRecipe(src.absolutePath)
            val dest = File(tmp, used)
            assertTrue(dest.exists())
            assertContentEquals(origBytes, dest.readBytes(), "import must be lossless")
            assertTrue(File(tmp, "index.html").exists())
        } finally {
            tmp.deleteRecursively()
        }
    }

    private fun assumeCorpus() {
        if (!corpusDir.exists()) {
            fail("corpus not found at ${corpusDir.absolutePath} (cwd=${System.getProperty("user.dir")})")
        }
    }

    private fun normalizeWs(s: String): String =
        s.replace(Regex("\\s+"), " ").trim()
}
