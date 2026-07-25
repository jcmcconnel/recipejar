package recipejar.html

import recipejar.StringProcessor
import recipejar.domain.Recipe
import recipejar.recipe.Ingredient

/**
 * HTML compatibility layer for Recipe.
 * Ports token logic, parse/serialize from AbstractXHTMLBasedFile + RecipeFile.
 * Always re-applies template structure for headers/footers (per design feedback).
 * Supports smart active footer variants:
 *  - "browser-footer" : full for on-disk resting / browser view (default for save)
 *  - "program-footer" : app-internal (labels only per template, with links)
 *  - "export-footer" : for export variant
 * Loading always extracts core regardless of footer present in source.
 * No data loss for core fields on roundtrip (meta times updated on real save).
 */
object RecipeSerializer {

    private const val DOCTYPE =
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"

    // Fixed template fragments for *-header/footer always re-applied (from recipe.template)
    private const val NOTES_HEADER = "<div id=\"notes-header\"><h3>Notes:</h3></div>"
    private const val NOTES_FOOTER = "<div id=\"notes-footer\"></div>"
    private const val INGREDIENTS_HEADER = "<div id=\"ingredients-header\"><h3>You will need:</h3></div>"
    private const val PROCEDURE_HEADER = "<div id=\"procedure-header\"><h3>Procedure:</h3></div>"

    private const val PROGRAM_FOOTER = """<div id="program-footer">
		  <br/>
		  <hr/>
        [LABELS]
    </div>"""

    private const val BROWSER_FOOTER = """<div id="browser-footer">
		  <br/>
		  <hr/>
		  Labels: [LABELS]<br/>
	      Last Saved: [LASTSAVE]<br/>
	      Created: [CREATED]<br/>
	      By: [AUTHOR]<br/>
	      Using: <a href="http://code.google.com/p/recipejar/">[ABOUT] [VERSION]</a>.
	      <hr/>
         <a href="index.html">Index</a>
    </div>"""

    private const val EXPORT_FOOTER = """<div id="export-footer">
		  <br/>
		  <hr/>
		  Labels: [LABELS]<br/>
	      Last Saved: [CURRENT-TIME]<br/>
	      Created: [CREATED]<br/>
	      By: [AUTHOR]<br/>
	      Using: <a href="http://code.google.com/p/recipejar/">[ABOUT] [VERSION]</a>.
	      <hr/>
    </div>"""

    /**
     * Parse HTML (from on-disk or string) into domain Recipe.
     * Extracts title, div sections by id, labels from meta, ingredients via structured spans.
     * Ignores presentation footers/headers for model (they get re-applied on serialize).
     */
    fun parse(html: String): Recipe {
        val recipe = Recipe()
        // Title from <title> preferred (more reliable)
        val titleMatch = Regex("(?i)<title>(.*?)</title>").find(html)
        recipe.title = StringProcessor.removeCarriageReturns(titleMatch?.groupValues?.get(1)?.trim() ?: "")

        // Metas - collect all, last for dups as map semantics. Improved regex tolerates ws around = and ' or " (minimal port of Element attr logic).
        val metaRe = Regex("(?i)<meta\\s+([^>]+?)(?:/?>)")
        metaRe.findAll(html).forEach { m ->
            val attrs = m.groupValues[1]
            val nameMatch = Regex("(?i)(?:name|http-equiv)\\s*=\\s*[\"']([^\"']+)[\"']").find(attrs)
            val name = nameMatch?.groupValues?.get(1)?.lowercase()
            val contentMatch = Regex("(?i)content\\s*=\\s*[\"']([^\"']*)[\"']").find(attrs)
            val content = contentMatch?.groupValues?.get(1) ?: ""
            if (name != null) {
                recipe.meta[name] = content
            }
        }
        val labelsStr = recipe.meta["labels"] ?: ""
        recipe.setLabels(labelsStr)

        // Core sections - use div id extraction (ported balancer logic)
        recipe.notes = StringProcessor.removeCarriageReturns(extractDivContent(html, "notes"))
        recipe.procedure = StringProcessor.removeCarriageReturns(extractDivContent(html, "procedure"))

        val ingredContent = StringProcessor.removeCarriageReturns(extractDivContent(html, "ingredients"))
        recipe.ingredients.clear()
        recipe.ingredients.addAll(parseIngredients(ingredContent))

        // also try header h1 if title missing
        if (recipe.title.isBlank()) {
            val h1Match = Regex("(?i)<div id=\"header\">\\s*<h1>(.*?)</h1>").find(html)
            if (h1Match != null) recipe.title = StringProcessor.removeCarriageReturns(h1Match.groupValues[1].trim())
        }
        return recipe
    }

    private fun parseIngredients(ingredHtml: String): List<Ingredient> {
        val ings = mutableListOf<Ingredient>()
        // Find each <li>...</li> inner
        val liRe = Regex("(?i)<li>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)
        liRe.findAll(ingredHtml).forEach { match ->
            val inner = match.groupValues[1]
            val ing = Ingredient.parse(inner)
            // include even if partial, like original
            ings.add(ing)
        }
        return ings
    }

    /**
     * Port of parseForContentAsText + tokenize by id for divs.
     * Simple stack-based extraction to handle nested like ul inside ingredients.
     */
    private fun extractDivContent(html: String, id: String): String {
        val idRe = Regex("(?i)<div[^>]*id\\s*=\\s*[\"']$id[\"'][^>]*>")
        val startMatch = idRe.find(html) ?: return ""
        val startAfter = startMatch.range.last + 1
        return extractElementContent(html, startAfter, "div")
    }

    private fun extractElementContent(html: String, startAfter: Int, tag: String): String {
        val content = StringBuilder()
        var i = startAfter
        var stack = 1
        val lower = tag.lowercase()
        while (i < html.length && stack > 0) {
            val ch = html[i]
            if (ch == '<') {
                if (i + 1 < html.length && (html[i + 1] == '!' || html[i + 1] == '?')) {
                    // minimal port of loseCommentsEtc (skip <!-- --> / <?> inside content; harmless for our corpus)
                    i += 2
                    while (i < html.length && html[i] != '>') i++
                    if (i < html.length) i++
                    continue
                }
                if (i + 1 < html.length && html[i + 1] == '/') {
                    // end tag
                    i += 2
                    val tbuf = StringBuilder()
                    while (i < html.length && html[i] != '>') {
                        tbuf.append(html[i])
                        i++
                    }
                    if (i < html.length) i++
                    val t = tbuf.toString().trim().lowercase()
                    if (t == lower) {
                        stack--
                        if (stack == 0) return content.toString()
                    } else {
                        content.append("</").append(tbuf).append(">")
                    }
                } else {
                    // open tag
                    i++
                    val tbuf = StringBuilder()
                    while (i < html.length && !html[i].isWhitespace() && html[i] != '>') {
                        tbuf.append(html[i])
                        i++
                    }
                    val t = tbuf.toString().lowercase()
                    if (t == lower) stack++
                    content.append("<").append(tbuf)
                    while (i < html.length && html[i] != '>') {
                        content.append(html[i])
                        i++
                    }
                    if (i < html.length) {
                        content.append(html[i])
                        i++
                    }
                }
            } else {
                content.append(ch)
                i++
            }
        }
        return content.toString()
    }

    /**
     * Serialize to exact XHTML using always-re-apply template.
     * Core content from model, headers/footers from canonical template fragments.
     * activeFooter chooses variant for "smart" roundtripping:
     *   resting/browser on-disk use browser-footer (default)
     *   app view can use program-footer (stripped nav)
     */
    fun serialize(recipe: Recipe, activeFooter: String = "browser-footer"): String {
        val sb = StringBuilder()
        sb.append(DOCTYPE).append("\n<html>\n  <head>\n    ")

        // Metas: always include core set + from model (labels separate)
        sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n    ")
        sb.append("<meta name=\"generator\" content=\"RecipeJar\"/>\n    ")
        if (recipe.labels.isNotEmpty()) {
            sb.append("<meta name=\"labels\" content=\"").append(recipe.getLabelsAsString()).append("\"/>\n    ")
        }
        // Emit remaining metas from parsed (preserves extra/dups last-wins semantics; order not guaranteed as map, but all keys roundtripped for fidelity)
        val emitted = mutableSetOf("content-type", "generator", "labels")
        recipe.meta.forEach { (k, v) ->
            val key = k.lowercase()
            if (!emitted.contains(key) && v.isNotEmpty()) {
                emitted.add(key)
                if (key == "content-type" || key == "http-equiv") {
                    sb.append("<meta http-equiv=\"").append(k).append("\" content=\"").append(v).append("\"/>\n    ")
                } else {
                    sb.append("<meta name=\"").append(k).append("\" content=\"").append(v).append("\"/>\n    ")
                }
            }
        }
        // title and style (canonical)
        sb.append("<title>").append(escapeForAttr(recipe.title)).append("</title>\n    ")
        sb.append("<style type=\"text/css\">@import url(\"style/default.css\");</style>\n    \n  </head>\n   <body>\n")

        // header
        sb.append("    <div id=\"header\"><h1>").append(recipe.title).append("</h1></div>    ").append(NOTES_HEADER).append("\n")
        sb.append("\n    <div id=\"notes\">").append(recipe.notes).append("</div>")
        sb.append("\n    ").append(NOTES_FOOTER)
        sb.append("\n    ").append(INGREDIENTS_HEADER)
        sb.append("\n    <div id=\"ingredients\">")
        sb.append(buildIngredientsAsHtml(recipe.ingredients))
        sb.append("\n    </div>")
        sb.append("\n    ").append(PROCEDURE_HEADER)
        sb.append("\n    <div id=\"procedure\">").append(recipe.procedure).append("</div>")

        // active footer, with macros processed
        val footerTmpl = when (activeFooter) {
            "program-footer" -> PROGRAM_FOOTER
            "export-footer" -> EXPORT_FOOTER
            else -> BROWSER_FOOTER  // browser or default
        }
        val processedFooter = processMacros(footerTmpl, recipe, activeFooter)
        sb.append("\n    ").append(processedFooter).append("\n  </body>\n</html>\n")
        return sb.toString()
    }

    private fun buildIngredientsAsHtml(ings: List<Ingredient>): String {
        val sb = StringBuilder("\n      <ul>\n")
        // Port exact removal from orig RecipeFile.getIngredientsAsHTML: remove name-empty (but only non-last in loop); for simplicity+roundtrip fidelity here, emit all parsed (including partial/empty name) as original parser keeps them.
        ings.forEach { ing ->
            sb.append(ing.toXHTMLString())
        }
        sb.append("      </ul>")
        return sb.toString()
    }

    /**
     * Port of processMacros + getMacroText for recipe.
     */
    private fun processMacros(macroString: String, recipe: Recipe, activeFooter: String): String {
        try {
            val textStroke = StringBuilder()
            var cIdx = 0
            while (cIdx < macroString.length) {
                val c = macroString[cIdx]
                if (c == '[') {
                    val s = StringBuilder()
                    s.append(c)
                    cIdx++
                    while (cIdx < macroString.length && macroString[cIdx] != ']') {
                        if (macroString[cIdx] == '[') {
                            textStroke.append(s)
                            s.clear()
                            s.append('[')
                        }
                        s.append(macroString[cIdx])
                        cIdx++
                    }
                    if (cIdx < macroString.length) {
                        s.append(macroString[cIdx])
                        cIdx++
                    }
                    val macroU = s.toString().uppercase()
                    when (macroU) {
                        "[TITLE]" -> textStroke.append(recipe.title)
                        "[LASTSAVE]" -> {
                            val m = recipe.meta["last saved"]
                            textStroke.append(m ?: "")
                        }
                        "[CREATED]" -> {
                            val m = recipe.meta["created"]
                            textStroke.append(m ?: "Unknown.")
                        }
                        "[LABELS]" -> {
                            val isExport = activeFooter == "export-footer"
                            textStroke.append(getMacroLabels(recipe, isExport))
                        }
                        "[AUTHOR]" -> {
                            val m = recipe.meta["author"]
                            textStroke.append(m ?: "Unknown")
                        }
                        "[USERPHONE]" -> {
                            val m = recipe.meta["userphone"]
                            textStroke.append(m ?: "Unlisted")
                        }
                        "[USEREMAIL]" -> {
                            val m = recipe.meta["useremail"]
                            textStroke.append(m ?: "Unlisted")
                        }
                        "[CUSTOM]" -> {
                            val m = recipe.meta["custom"]
                            textStroke.append(m ?: "")
                        }
                        "[VERSION]" -> textStroke.append("RecipeJar")
                        "[CURRENT-TIME]" -> textStroke.append(recipe.meta["last saved"] ?: "Sometime before")
                        "[ABOUT]" -> textStroke.append("RecipeJar")
                        else -> textStroke.append(getMacroText(s.toString(), recipe, activeFooter))
                    }
                } else {
                    textStroke.append(c)
                    cIdx++
                }
            }
            return textStroke.toString()
        } catch (ex: Exception) {
            // port of original processMacros (swallows to return raw); narrowed would hide less but keep parity for bad macro input in corpus
            return macroString
        }
    }

    private fun getMacroLabels(recipe: Recipe, isExport: Boolean): String {
        if (recipe.labels.isEmpty()) {
            return "Currently None."
        }
        return recipe.labels.joinToString(", ") { label ->
            val trimmed = label.trim()
            if (isExport) {
                trimmed
            } else {
                "<a href=\"index.html#${StringProcessor.underscoreSpaces(trimmed)}\">$trimmed</a>"
            }
        }
    }

    private fun getMacroText(macro: String, recipe: Recipe, activeFooter: String): String {
        val upper = macro.uppercase()
        if (upper == "[LABELS]") {
            return getMacroLabels(recipe, activeFooter == "export-footer")
        }
        // others fallback to raw or basic
        return macro
    }

    private fun escapeForAttr(s: String): String {
        return s.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;")
    }
}
