package recipejar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HtmlFragmentTest {

    @Test
    fun brAndEntities_becomeReadableText() {
        val ann = htmlFragmentToAnnotatedString("Heat to 350&deg;F.<br/>Mix well.")
        assertEquals("Heat to 350°F.\nMix well.", ann.text)
    }

    @Test
    fun strongAndEm_applySpanStyles() {
        val ann = htmlFragmentToAnnotatedString("plain <strong>bold</strong> and <em>ital</em>")
        assertEquals("plain bold and ital", ann.text)
        val boldSpan = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end) == "bold"
        }
        assertNotNull(boldSpan)
        assertEquals(FontWeight.Bold, boldSpan.item.fontWeight)

        val italSpan = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end) == "ital"
        }
        assertNotNull(italSpan)
        assertEquals(FontStyle.Italic, italSpan.item.fontStyle)
    }

    @Test
    fun underlineSpan_and_colorSpan_fromMacros() {
        val ann = htmlFragmentToAnnotatedString(
            """<span style="text-decoration: underline;">u</span> """ +
                """<span style="color: #ff0000;">red</span>""",
        )
        assertEquals("u red", ann.text)
        val uSpan = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end) == "u"
        }
        assertNotNull(uSpan)
        assertEquals(TextDecoration.Underline, uSpan.item.textDecoration)

        val redSpan = ann.spanStyles.firstOrNull {
            ann.text.substring(it.start, it.end) == "red"
        }
        assertNotNull(redSpan)
        assertEquals(Color.Red, redSpan.item.color)
    }

    @Test
    fun nestedMacroSelection_boldAroundWords() {
        val ann = htmlFragmentToAnnotatedString("<strong>very hot</strong> oil")
        assertEquals("very hot oil", ann.text)
        assertTrue(ann.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun paragraphAndLink_preserveStructure() {
        val ann = htmlFragmentToAnnotatedString(
            """<p>First</p><p>See <a href="https://example.com">here</a>.</p>""",
        )
        assertTrue(ann.text.contains("First"), ann.text)
        assertTrue(ann.text.contains("here"), ann.text)
        assertTrue(ann.text.contains("\n"), ann.text)
        // Link annotation present (URL annotation string or link)
        assertTrue(
            ann.getStringAnnotations(0, ann.length).isNotEmpty() ||
                ann.getLinkAnnotations(0, ann.length).isNotEmpty() ||
                ann.spanStyles.any {
                    ann.text.substring(it.start, it.end) == "here"
                },
            "expected link or styled span for anchor text: $ann",
        )
    }

    @Test
    fun stripSimpleHtml_stillUsedForTitles_stripsTags() {
        // Regression: plain strip path remains for titles / share text
        val plain = stripSimpleHtml("<strong>Title</strong><br/>Line")
        assertEquals("Title\nLine", plain)
        assertFalse(plain.contains("<"))
    }

    @Test
    fun parseCssColor_hexAndRgb() {
        assertEquals(Color.Red, parseCssColor("color: #ff0000"))
        assertEquals(Color(0xFF00, 0x80, 0x00), parseCssColorValue("#008000"))
        assertEquals(Color(10, 20, 30), parseCssColorValue("rgb(10, 20, 30)"))
    }

    @Test
    fun bananaBreadProcedure_rendersBreaksAndDegrees() {
        val html = """
            <p>Preheat oven to 350&deg;F.  Grease a 9x5 inch loaf pan.<br/>
            Mix together the dry ingredients.</p>
        """.trimIndent()
        val ann = htmlFragmentToAnnotatedString(html)
        assertTrue(ann.text.contains("350°F"), ann.text)
        assertTrue(ann.text.contains("Mix together"), ann.text)
        assertFalse(ann.text.contains("<br"), ann.text)
        assertFalse(ann.text.contains("&deg"), ann.text)
    }
}
