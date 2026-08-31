package recipejar.persistence

import recipejar.domain.Recipe
import recipejar.recipe.Ingredient
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.fail

/**
 * Drives shipped [FileSystemRecipeRepository.exportDirectoryZip] on a temp tree
 * (never mutates Test/Recipes).
 */
class ExportZipTest {

    @Test
    fun exportDirectoryZip_containsRecipeHtmlAndAssets() {
        val tmp = Files.createTempDirectory("rj-zip-src").toFile()
        val outDir = Files.createTempDirectory("rj-zip-out").toFile()
        try {
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            repo.saveRecipe(
                Recipe(
                    title = "Zip Cake",
                    notes = "n",
                    procedure = "bake",
                    ingredients = mutableListOf(Ingredient("1", "cup", "sugar")),
                    labels = mutableListOf("Dessert"),
                ),
            )
            // Supporting asset as present in a real recipe directory
            val styleDir = File(tmp, "style").also { it.mkdirs() }
            File(styleDir, "default.css").writeText("body{font-family:serif;}", Charsets.UTF_8)
            File(tmp, "readme.txt").writeText("family recipes", Charsets.UTF_8)

            val zipPath = File(outDir, "recipes-export.zip").absolutePath
            repo.exportDirectoryZip(zipPath)

            val zip = File(zipPath)
            assertTrue(zip.isFile && zip.length() > 0, "zip file written")

            ZipFile(zip).use { zf ->
                val names = zf.entries().asSequence().map { it.name.replace('\\', '/') }.toSet()
                assertTrue(names.any { it.endsWith("ZipCake.html") }, "recipe html in zip: $names")
                assertTrue(names.any { it.equals("index.html", ignoreCase = true) }, "index in zip: $names")
                assertTrue(names.any { it.contains("default.css") }, "css asset in zip: $names")
                assertTrue(names.any { it.endsWith("readme.txt") }, "supporting file in zip: $names")
                // entry contents are real HTML from the tree
                val htmlEntry = zf.entries().asSequence().first { it.name.endsWith("ZipCake.html") }
                zf.getInputStream(htmlEntry).bufferedReader(Charsets.UTF_8).use { reader ->
                    val body = reader.readText()
                    assertTrue(body.contains("Zip Cake") || body.contains("browser-footer"), body.take(200))
                }
            }
        } finally {
            tmp.deleteRecursively()
            outDir.deleteRecursively()
        }
    }

    @Test
    fun exportDirectoryZip_doesNotRequireMutatingCorpus() {
        val corpus = File("../Test/Recipes").let { if (it.exists()) it else File("Test/Recipes") }
        if (!corpus.isDirectory) {
            // Still prove API on empty temp
            val tmp = Files.createTempDirectory("rj-zip-empty").toFile()
            try {
                val repo = FileSystemRecipeRepository(tmp.absolutePath)
                val zip = File(tmp, "out.zip")
                repo.exportDirectoryZip(zip.absolutePath)
                assertTrue(zip.exists())
            } finally {
                tmp.deleteRecursively()
            }
            return
        }
        // Read-only path: zip a *copy* of one small file into temp, never write into corpus
        val tmp = Files.createTempDirectory("rj-zip-copy").toFile()
        try {
            val sample = corpus.listFiles { f -> f.isFile && f.name.endsWith(".html") && f.name != "index.html" }
                ?.firstOrNull() ?: fail("no html in corpus")
            sample.copyTo(File(tmp, sample.name))
            val repo = FileSystemRecipeRepository(tmp.absolutePath)
            val zipPath = File(tmp, "bundle.zip").absolutePath
            repo.exportDirectoryZip(zipPath)
            ZipFile(zipPath).use { zf ->
                val names = zf.entries().asSequence().map { it.name }.toList()
                assertTrue(names.any { it == sample.name }, names.toString())
                assertFalse(names.any { it == "bundle.zip" }, "zip must not nest itself")
            }
        } finally {
            tmp.deleteRecursively()
        }
    }
}
