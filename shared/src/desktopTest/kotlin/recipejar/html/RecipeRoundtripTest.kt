package recipejar.html

import recipejar.domain.Recipe
import java.io.File
import kotlin.test.*

/**
 * Roundtrip tests against original Test/ corpus goldens (kept intact).
 * Load parse -> model -> serialize (re-apply template + footer variant) -> reparse
 * Verify: no data loss on core (title, notes, procedure, ingredients, labels)
 * + browser compatible structure (doctype, div ids, footer present).
 * Use "browser-footer" for resting files.
 */
class RecipeRoundtripTest {

    private val corpusDir = listOf(
        File("../Test/Recipes"),        // when test cwd is shared/
        File("Test/Recipes"),           // gradle run from workspace root
        File("../../Test/Recipes")
    ).firstOrNull { it.exists() && it.isDirectory } ?: File("../Test/Recipes")

    @Test
    fun roundtrip_SimpleTest1() {
        roundtrip("Test1.html", useBrowserFooter = true)
    }

    @Test
    fun roundtrip_AppleSauceCobbler() {
        roundtrip("AppleSauceCobbler.html", useBrowserFooter = true)
    }

    @Test
    fun roundtrip_WithLabelsAndMarkup() {
        roundtrip("BananaBread.html", useBrowserFooter = true)
    }

    @Test
    fun roundtrip_AppViewVariant() {
        // smart: program-footer for app view
        roundtrip("Test1.html", useBrowserFooter = false)
    }

    private fun roundtrip(filename: String, useBrowserFooter: Boolean) {
        val origPath = File(corpusDir, filename)
        if (!origPath.exists()) {
            val alt = File("../Test/Recipes", filename)
            if (!alt.exists()) {
                println("Skipping $filename - corpus not found at expected path (tried $origPath)")
                return
            }
        }
        val htmlFile = if (origPath.exists()) origPath else File("../Test/Recipes", filename)
        val origHtml = htmlFile.readText(Charsets.UTF_8)

        val parsed: Recipe = RecipeSerializer.parse(origHtml)
        assertTrue(parsed.title.isNotBlank(), "title from $filename")

        val footer = if (useBrowserFooter) "browser-footer" else "program-footer"
        val serialized = RecipeSerializer.serialize(parsed, footer)

        // Browser compat structure
        assertTrue(serialized.startsWith("<!DOCTYPE html PUBLIC"), "doctype for browser")
        assertTrue(serialized.contains("<div id=\"header\">"), "header div")
        assertTrue(serialized.contains("<div id=\"notes\">"), "notes div")
        assertTrue(serialized.contains("<div id=\"ingredients\">"), "ingredients div")
        assertTrue(serialized.contains("<div id=\"procedure\">"), "procedure div")
        assertTrue(serialized.contains("id=\"$footer\""), "chosen footer variant $footer")

        // reparse for no data loss on core
        val reparsed = RecipeSerializer.parse(serialized)

        assertEquals(parsed.title, reparsed.title, "title roundtrip $filename")
        // notes/procedure: compare after ws normalize because template emit + original may have slight diffs
        assertEquals(normalizeWs(parsed.notes), normalizeWs(reparsed.notes), "notes roundtrip $filename")
        assertEquals(normalizeWs(parsed.procedure), normalizeWs(reparsed.procedure), "procedure $filename")
        assertEquals(parsed.labels, reparsed.labels, "labels $filename")

        assertEquals(parsed.ingredients.size, reparsed.ingredients.size, "ing count $filename")
        for (i in parsed.ingredients.indices) {
            val a = parsed.ingredients[i]
            val b = reparsed.ingredients[i]
            assertEquals(a.quantity.trim(), b.quantity.trim())
            assertEquals(a.unit.trim(), b.unit.trim())
            assertEquals(a.name.trim(), b.name.trim(), "ing name[$i] $filename")
        }
    }

    private fun normalizeWs(s: String): String =
        s.replace(Regex("\\s+"), " ").trim()
}
