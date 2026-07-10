package recipejar.persistence

import recipejar.StringProcessor
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Desktop FS implementation of [RecipeRepository].
 *
 * Patterns ported from original FileSystemRecipeRepository + IndexFile:
 * - title → filename via [StringProcessor.removeBadChars]
 * - labels → category lists under letter of the *label* name (underscore id keys)
 * - index rebuild from directory scan (safety net; matches IndexFile letter/category shape)
 * - import is lossless byte copy; export uses export-footer serialize variant
 */
class FileSystemRecipeRepository(override val location: String) : RecipeRepository {

    private val base: File = run {
        val f = File(location)
        if (f.isAbsolute) f else File(System.getProperty("user.dir"), location)
    }.absoluteFile

    init {
        if (!base.exists()) base.mkdirs()
    }

    override fun listRecipes(): List<String> {
        val files = base.listFiles { f: File ->
            f.isFile &&
                f.name.endsWith(".html", ignoreCase = true) &&
                !f.name.equals("index.html", ignoreCase = true) &&
                !f.name.startsWith("._")
        }
        return files?.map { it.name }?.sorted() ?: emptyList()
    }

    override fun loadRecipe(filename: String): Recipe {
        val f = File(base, filename)
        if (!f.exists()) throw IllegalArgumentException("Recipe not found: $filename in $base")
        val html = f.readText(Charsets.UTF_8)
        return RecipeSerializer.parse(html)
    }

    override fun saveRecipe(recipe: Recipe, originalFilename: String?) {
        if (recipe.title.isBlank()) {
            throw IllegalArgumentException("Cannot save recipe with blank title")
        }
        val safeName = filenameFor(recipe)
        val target = File(base, safeName)

        val ts = nowStr()
        recipe.meta["last saved"] = ts
        if (recipe.meta["created"].isNullOrBlank()) {
            recipe.meta["created"] = ts
        }

        val html = RecipeSerializer.serialize(recipe, "browser-footer")

        if (originalFilename != null && originalFilename != safeName) {
            val oldF = File(base, originalFilename)
            if (oldF.exists()) {
                try {
                    Files.move(
                        oldF.toPath(),
                        target.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (_: Exception) {
                    // Non-atomic FS fallback
                    oldF.copyTo(target, overwrite = true)
                    if (oldF.absolutePath != target.absolutePath) {
                        oldF.delete()
                    }
                }
            }
        }
        target.writeText(html, Charsets.UTF_8)
        updateIndex()
    }

    override fun deleteRecipe(filename: String) {
        val f = File(base, filename)
        if (f.exists()) f.delete()
        updateIndex()
    }

    override fun importRecipe(sourcePath: String): String {
        val src = File(sourcePath)
        if (!src.exists() || !src.isFile) {
            throw IllegalArgumentException("Import source not found: $sourcePath")
        }
        val html = src.readText(Charsets.UTF_8)
        val rec = RecipeSerializer.parse(html)
        if (rec.title.isBlank()) {
            throw IllegalArgumentException("Import source has no title: $sourcePath")
        }
        val targetName = filenameFor(rec)
        val dest = File(base, targetName)
        // Lossless: copy original bytes (no re-serialize)
        src.copyTo(dest, overwrite = true)
        updateIndex()
        return targetName
    }

    override fun exportRecipe(filename: String, targetPath: String) {
        val rec = loadRecipe(filename)
        val html = RecipeSerializer.serialize(rec, "export-footer")
        File(targetPath).writeText(html, Charsets.UTF_8)
    }

    /** Public for tests / callers that need the same naming rule as save. */
    fun filenameFor(recipe: Recipe): String =
        StringProcessor.removeBadChars(recipe.title) + ".html"

    private fun updateIndex() {
        val entries = listRecipes().mapNotNull { fn ->
            try {
                val r = loadRecipe(fn)
                if (r.title.isBlank()) null else fn to r
            } catch (_: Exception) {
                null
            }
        }
        val indexHtml = buildIndexHtml(entries)
        File(base, "index.html").writeText(indexHtml, Charsets.UTF_8)
    }

    private fun nowStr(): String =
        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).format(Date())

    private fun firstLetter(s: String): Char {
        val t = s.trim()
        if (t.isEmpty()) return '0'
        val c = t[0].uppercaseChar()
        return if (c in 'A'..'Z') c else '0'
    }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    /**
     * Rebuild index.html in the shape of original IndexFile output:
     * header, per-letter sections (A–Z + letter0/"Other"), DEFAULT ul + category uls,
     * section-footer / section-header separators. Categories live under the letter of
     * the *label* name (not the recipe title), matching original IndexFile behavior.
     */
    private fun buildIndexHtml(entries: List<Pair<String, Recipe>>): String {
        val defaults = mutableMapOf<Char, MutableList<Pair<String, String>>>()
        val labelsByLet = mutableMapOf<Char, MutableMap<String, MutableList<Pair<String, String>>>>()

        entries.forEach { (fn, rec) ->
            val let = firstLetter(rec.title)
            defaults.getOrPut(let) { mutableListOf() }.add(fn to rec.title)
            rec.labels.forEach { raw ->
                val lbl = raw.trim()
                if (lbl.isNotEmpty()) {
                    val llet = firstLetter(lbl)
                    val map = labelsByLet.getOrPut(llet) { mutableMapOf() }
                    // Key = underscored id (as IndexFile category keys)
                    val key = StringProcessor.underscoreSpaces(lbl)
                    map.getOrPut(key) { mutableListOf() }.add(fn to rec.title)
                }
            }
        }

        val sb = StringBuilder()
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")
        sb.append("<html>\n  <head>\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n")
        sb.append("    <meta name=\"generator\" content=\"RecipeJar\"/>\n")
        sb.append("    <meta name=\"last saved\" content=\"").append(escapeHtml(nowStr())).append("\"/>\n")
        sb.append("    <meta name=\"created\" content=\"Prior to: ").append(escapeHtml(nowStr())).append("\"/>\n")
        sb.append("    <title>Index</title>\n")
        sb.append("    <style type=\"text/css\">@import url(\"style/index.css\");</style>\n    \n  </head>\n  <body>\n")
        sb.append("    <div id=\"header\"><h1>Index</h1></div>\n")
        sb.append("      <div class=\"section-header\"></div>\n")

        fun appendLetter(let: Char) {
            val id = if (let == '0') "letter0" else "letter$let"
            val heading = if (let == '0') "Other" else let.toString()
            val defs = (defaults[let] ?: emptyList()).sortedBy { it.second.lowercase(Locale.US) }
            val labs = labelsByLet[let]?.toSortedMap() ?: emptyMap()

            sb.append("    <div id=\"").append(id).append("\"><h2>").append(heading).append("</h2>")
            if (defs.isNotEmpty()) {
                sb.append("\n      <ul>\n")
                defs.forEach { (fn, tit) ->
                    sb.append("        <li><a href=\"").append(escapeHtml(fn)).append("\">")
                        .append(escapeHtml(tit)).append("</a></li>\n")
                }
                sb.append("      </ul>\n")
            }
            labs.forEach { (key, items) ->
                val display = StringProcessor.spaceUnderscores(key)
                val sorted = items.sortedBy { it.second.lowercase(Locale.US) }
                sb.append("\n      <ul id=\"").append(escapeHtml(key)).append("\"><h3>")
                    .append(escapeHtml(display)).append("</h3>\n")
                sorted.forEach { (fn, tit) ->
                    sb.append("        <li><a href=\"").append(escapeHtml(fn)).append("\">")
                        .append(escapeHtml(tit)).append("</a></li>\n")
                }
                sb.append("      </ul>\n")
            }
            if (defs.isEmpty() && labs.isEmpty()) {
                sb.append("    ")
            }
            sb.append("</div>\n")
            sb.append("      <div class=\"section-footer\"><a href=\"#header\">^Back to Top</a></div>\n")
            sb.append("      <div class=\"section-header\"></div>\n")
        }

        for (c in 'A'..'Z') appendLetter(c)
        appendLetter('0')

        sb.append("  </body>\n</html>\n")
        return sb.toString()
    }
}
