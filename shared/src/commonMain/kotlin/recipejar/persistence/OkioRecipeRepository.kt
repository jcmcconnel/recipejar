package recipejar.persistence

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import recipejar.StringProcessor
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer

/**
 * Multiplatform recipe directory repository using Okio.
 * Same HTML + index layout as the desktop [FileSystemRecipeRepository].
 *
 * [location] is an absolute filesystem path string for the library root.
 */
class OkioRecipeRepository(
    override val location: String,
    private val fs: FileSystem = FileSystem.SYSTEM,
) : RecipeRepository {

    private val base: Path = location.toPath()

    init {
        if (!fs.exists(base)) {
            fs.createDirectories(base)
        }
    }

    override fun listRecipes(): List<String> {
        if (!fs.exists(base) || !fs.metadata(base).isDirectory) return emptyList()
        return fs.list(base)
            .map { it.name }
            .filter { name ->
                name.endsWith(".html", ignoreCase = true) &&
                    !name.equals("index.html", ignoreCase = true) &&
                    !name.startsWith("._") &&
                    !name.startsWith("rj-preview-")
            }
            .sorted()
    }

    override fun loadRecipe(filename: String): Recipe {
        val f = base / filename
        if (!fs.exists(f)) throw IllegalArgumentException("Recipe not found: $filename in $location")
        val html = fs.read(f) { readUtf8() }
        return RecipeSerializer.parse(html)
    }

    /** Raw HTML for reader/WebView without re-parse loss. */
    fun loadRecipeHtml(filename: String): String {
        val f = base / filename
        if (!fs.exists(f)) throw IllegalArgumentException("Recipe not found: $filename")
        return fs.read(f) { readUtf8() }
    }

    override fun saveRecipe(recipe: Recipe, originalFilename: String?) {
        if (recipe.title.isBlank()) {
            throw IllegalArgumentException("Cannot save recipe with blank title")
        }
        val safeName = filenameFor(recipe)
        val target = base / safeName
        val ts = currentTimestampString()
        recipe.meta["last saved"] = ts
        if (recipe.meta["created"].isNullOrBlank()) {
            recipe.meta["created"] = ts
        }
        val html = RecipeSerializer.serialize(recipe, "browser-footer")
        if (originalFilename != null && originalFilename != safeName) {
            val old = base / originalFilename
            if (fs.exists(old)) {
                if (fs.exists(target)) fs.delete(target)
                fs.atomicMove(old, target)
            }
        }
        fs.write(target) { writeUtf8(html) }
        updateIndex()
    }

    override fun deleteRecipe(filename: String) {
        val f = base / filename
        if (fs.exists(f)) fs.delete(f)
        updateIndex()
    }

    override fun importRecipe(sourcePath: String): String {
        val src = sourcePath.toPath()
        if (!fs.exists(src) || fs.metadata(src).isDirectory) {
            throw IllegalArgumentException("Import source not found: $sourcePath")
        }
        val html = fs.read(src) { readUtf8() }
        val rec = RecipeSerializer.parse(html)
        if (rec.title.isBlank()) {
            throw IllegalArgumentException("Import source has no title: $sourcePath")
        }
        val targetName = filenameFor(rec)
        val dest = base / targetName
        fs.copy(src, dest)
        updateIndex()
        return targetName
    }

    /** Import from already-read HTML bytes (document picker path). */
    fun importHtmlBytes(htmlUtf8: String, suggestedName: String? = null): String {
        val rec = RecipeSerializer.parse(htmlUtf8)
        if (rec.title.isBlank()) {
            throw IllegalArgumentException("Import HTML has no title")
        }
        val targetName = filenameFor(rec)
        fs.write(base / targetName) { writeUtf8(htmlUtf8) }
        updateIndex()
        return targetName
    }

    override fun exportRecipe(filename: String, targetPath: String) {
        val rec = loadRecipe(filename)
        val html = RecipeSerializer.serialize(rec, "export-footer")
        val dest = targetPath.toPath()
        dest.parent?.let { if (!fs.exists(it)) fs.createDirectories(it) }
        fs.write(dest) { writeUtf8(html) }
    }

    /** Export-footer HTML string for share sheet (no intermediate path required). */
    fun exportRecipeHtml(filename: String): String {
        val rec = loadRecipe(filename)
        return RecipeSerializer.serialize(rec, "export-footer")
    }

    fun filenameFor(recipe: Recipe): String =
        StringProcessor.removeBadChars(recipe.title) + ".html"

    fun isEmptyLibrary(): Boolean = listRecipes().isEmpty()

    private fun updateIndex() {
        val entries = listRecipes().mapNotNull { fn ->
            try {
                val r = loadRecipe(fn)
                if (r.title.isBlank()) null else fn to r
            } catch (_: Exception) {
                null
            }
        }
        fs.write(base / "index.html") { writeUtf8(buildIndexHtml(entries)) }
    }

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

    private fun buildIndexHtml(entries: List<Pair<String, Recipe>>): String {
        val defaults = mutableMapOf<Char, MutableList<Pair<String, String>>>()
        val labelsByLet = mutableMapOf<Char, MutableMap<String, MutableList<Pair<String, String>>>>()

        for (pair in entries) {
            val fn = pair.first
            val rec = pair.second
            val let = firstLetter(rec.title)
            defaults.getOrPut(let) { mutableListOf() }.add(fn to rec.title)
            for (raw in rec.labels) {
                val lbl = raw.trim()
                if (lbl.isNotEmpty()) {
                    val llet = firstLetter(lbl)
                    val map = labelsByLet.getOrPut(llet) { mutableMapOf() }
                    val key = StringProcessor.underscoreSpaces(lbl)
                    map.getOrPut(key) { mutableListOf() }.add(fn to rec.title)
                }
            }
        }

        val sb = StringBuilder()
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n")
        sb.append("<html>\n  <head>\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n")
        sb.append("    <meta name=\"generator\" content=\"RecipeJar\"/>\n")
        sb.append("    <meta name=\"last saved\" content=\"").append(escapeHtml(currentTimestampString())).append("\"/>\n")
        sb.append("    <title>Index</title>\n")
        sb.append("    <style type=\"text/css\">@import url(\"style/index.css\");</style>\n  </head>\n  <body>\n")
        sb.append("    <div id=\"header\"><h1>Index</h1></div>\n")

        fun appendLetter(let: Char) {
            val id = if (let == '0') "letter0" else "letter$let"
            val heading = if (let == '0') "Other" else let.toString()
            val defs = (defaults[let] ?: emptyList()).sortedBy { pair: Pair<String, String> ->
                pair.second.lowercase()
            }
            val labsMap = labelsByLet[let] ?: emptyMap()
            val labKeys = labsMap.keys.sorted()
            sb.append("    <div id=\"").append(id).append("\"><h2>").append(heading).append("</h2>")
            if (defs.isNotEmpty()) {
                sb.append("\n      <ul>\n")
                for (def in defs) {
                    sb.append("        <li><a href=\"").append(escapeHtml(def.first)).append("\">")
                        .append(escapeHtml(def.second)).append("</a></li>\n")
                }
                sb.append("      </ul>\n")
            }
            for (key in labKeys) {
                val items = labsMap[key] ?: continue
                val display = StringProcessor.spaceUnderscores(key)
                val sorted = items.sortedBy { pair: Pair<String, String> -> pair.second.lowercase() }
                sb.append("\n      <ul id=\"").append(escapeHtml(key)).append("\"><h3>")
                    .append(escapeHtml(display)).append("</h3>\n")
                for (item in sorted) {
                    sb.append("        <li><a href=\"").append(escapeHtml(item.first)).append("\">")
                        .append(escapeHtml(item.second)).append("</a></li>\n")
                }
                sb.append("      </ul>\n")
            }
            sb.append("</div>\n")
        }

        for (c in 'A'..'Z') appendLetter(c)
        appendLetter('0')
        sb.append("  </body>\n</html>\n")
        return sb.toString()
    }
}

/**
 * Seed an empty library from bundled sample HTML pairs (filename → full HTML).
 * No-op when the library already has recipes. Does not overwrite existing files.
 */
fun OkioRecipeRepository.seedIfEmpty(samples: List<Pair<String, String>>) {
    if (!isEmptyLibrary()) return
    val fs = FileSystem.SYSTEM
    val root = location.toPath()
    for ((name, html) in samples) {
        val dest = root / name
        if (!fs.exists(dest)) {
            fs.write(dest) { writeUtf8(html) }
        }
    }
    val first = listRecipes().firstOrNull() ?: return
    val r = loadRecipe(first)
    saveRecipe(r, originalFilename = first)
}
