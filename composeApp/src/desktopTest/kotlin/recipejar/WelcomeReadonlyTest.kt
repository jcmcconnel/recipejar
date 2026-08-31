package recipejar

import androidx.compose.ui.text.font.FontWeight
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Welcome uses the shipped read-only styled-fragment path
 * ([welcomeHtmlForReadonly] + [htmlFragmentToAnnotatedString]), same as recipe notes.
 */
class WelcomeReadonlyTest {

    private fun bundledWelcomeHtml(): String {
        val fromClasspath = Thread.currentThread().contextClassLoader
            ?.getResourceAsStream("welcome.html")
            ?: javaClass.classLoader.getResourceAsStream("welcome.html")
            ?: javaClass.getResourceAsStream("/welcome.html")
        if (fromClasspath != null) {
            return fromClasspath.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
        val files = listOf(
            File("src/commonMain/resources/welcome.html"),
            File("composeApp/src/commonMain/resources/welcome.html"),
        )
        return files.first { it.isFile }.readText(Charsets.UTF_8)
    }

    @Test
    fun bundledWelcome_rendersCopyAndHelpLink_notRawTags() {
        val html = bundledWelcomeHtml()
        assertTrue(html.contains("<h1>"), "bundled file is HTML")
        val fragment = welcomeHtmlForReadonly(html)
        assertFalse(fragment.contains("<html>", ignoreCase = true))
        assertFalse(fragment.contains("<body>", ignoreCase = true))
        assertTrue(fragment.contains("<h1>RecipeJar</h1>") || fragment.contains("RecipeJar"))

        val ann = htmlFragmentToAnnotatedString(fragment)
        val text = ann.text
        assertTrue(text.contains("RecipeJar"), text)
        assertTrue(text.contains("Welcome"), text)
        assertTrue(text.contains("the help site"), text)
        assertTrue(text.contains("James McConnel"), text)
        assertTrue(text.contains("Preferences"), text)
        assertFalse(text.contains("<h1>"), text)
        assertFalse(text.contains("<br"), text)
        assertFalse(text.contains("<a "), text)
        assertFalse(text.contains("</"), text)

        val helpSpan = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end) == "the help site"
        }
        assertTrue(
            helpSpan != null ||
                ann.getLinkAnnotations(0, ann.length).isNotEmpty() ||
                ann.getStringAnnotations(0, ann.length).isNotEmpty(),
            "help link must be styled or annotated: $ann",
        )
        val heading = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end).contains("RecipeJar") &&
                it.item.fontWeight == FontWeight.Bold
        }
        assertTrue(heading != null, "h1 RecipeJar should be bold: ${ann.spanStyles}")
    }

    @Test
    fun styledFragment_headingsLinksBreaks_samePathAsRecipeNotes() {
        val fragment = welcomeHtmlForReadonly(
            """
            <html><body>
            <h1>Hello</h1>
            Line one<br/>Line two
            <a href="https://github.com/jcmcconnel/recipejar">the help site</a>
            </body></html>
            """.trimIndent(),
        )
        val ann = htmlFragmentToAnnotatedString(fragment)
        assertTrue(ann.text.contains("Hello"), ann.text)
        assertTrue(ann.text.contains("Line one"), ann.text)
        assertTrue(ann.text.contains("Line two"), ann.text)
        assertTrue(ann.text.contains("\n"), ann.text)
        assertTrue(ann.text.contains("the help site"), ann.text)
        assertFalse(ann.text.contains("<h1>"), ann.text)
        assertFalse(ann.text.contains("<br"), ann.text)
        assertTrue(ann.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(
            ann.getLinkAnnotations(0, ann.length).isNotEmpty() ||
                ann.spanStyles.any {
                    ann.text.substring(it.start, it.end) == "the help site"
                },
        )
    }

    @Test
    fun welcomePane_usesFragmentRenderer_notWebView() {
        val app = File("src/commonMain/kotlin/recipejar/App.kt").takeIf { it.isFile }
            ?: File("composeApp/src/commonMain/kotlin/recipejar/App.kt")
        val src = app.readText()
        val paneStart = src.indexOf("fun WelcomePane(")
        assertTrue(paneStart >= 0, "WelcomePane present")
        val paneEnd = src.indexOf("internal fun stripSimpleHtml", paneStart)
        val pane = if (paneEnd > paneStart) src.substring(paneStart, paneEnd) else src.substring(paneStart)
        assertTrue(pane.contains("htmlFragmentToAnnotatedString"), "welcome uses styled fragment path")
        assertTrue(pane.contains("welcomeHtmlForReadonly"), "welcome extracts body first")
        assertFalse(pane.contains("RecipeHtmlWebView"), "WebView is not the welcome primary look")
        assertFalse(pane.contains("stripSimpleHtml("), "welcome is not a tag-stripped dump")
    }
}
