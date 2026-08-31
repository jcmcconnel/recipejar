package recipejar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import recipejar.domain.Recipe
import recipejar.html.RecipeSerializer
import recipejar.macro.MacroIo
import recipejar.macro.MacroProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Macros applied through the shipped processor, then rendered with the
 * readonly HTML-fragment path (not raw source).
 */
class MacroReadonlyStylesTest {

    @Test
    fun selectionWrapAndStyledInsert_visibleInReadonlyFragment() {
        val bold = MacroIo.DEFAULT_MACROS.first { it.name == "Bold" }
        val ital = MacroIo.DEFAULT_MACROS.first { it.name == "Italics" }
        val under = MacroIo.DEFAULT_MACROS.first { it.name == "Underline" }
        val color = MacroIo.DEFAULT_MACROS.first { it.name == "Color" }
        val para = MacroIo.DEFAULT_MACROS.first { it.name == "Paragraph" }

        val bolded = MacroProcessor.applyMacro(bold.text, "hot")
        val italicized = MacroProcessor.applyMacro(ital.text, "gently")
        val underlined = MacroProcessor.applyMacro(under.text, "important")
        val colored = MacroProcessor.applyMacro(
            color.text,
            "warning",
            colorProvider = { "#ff0000" },
        )
        val deg = MacroProcessor.applyMacro("&deg;F", "")
        val paragraph = MacroProcessor.applyMacro(para.text, "First step.")

        assertEquals("<strong>hot</strong>", bolded)
        assertEquals("<em>gently</em>", italicized)
        assertNotNull(underlined)
        assertEquals("""<span style="color: #ff0000;">warning</span>""", colored)
        assertEquals("&deg;F", deg)
        assertEquals("<p>First step.</p>", paragraph)

        val notes = listOfNotNull(bolded, italicized, underlined).joinToString(" ")
        val procedure = listOfNotNull(colored, deg, paragraph).joinToString("<br/>")
        val recipe = Recipe(
            title = "Macro Roast",
            notes = notes,
            procedure = procedure,
        )
        val html = RecipePreviewHtml.forReadonly(recipe)
        assertTrue(html.contains("program-footer"))
        val parsed = RecipeSerializer.parse(html)

        val notesAnn = htmlFragmentToAnnotatedString(parsed.notes)
        assertEquals("hot gently important", notesAnn.text)
        assertTrue(
            notesAnn.spanStyles.any {
                notesAnn.text.substring(it.start, it.end) == "hot" &&
                    it.item.fontWeight == FontWeight.Bold
            },
            "bold span missing: ${notesAnn.spanStyles}",
        )
        assertTrue(
            notesAnn.spanStyles.any {
                notesAnn.text.substring(it.start, it.end) == "gently" &&
                    it.item.fontStyle == FontStyle.Italic
            },
        )
        assertTrue(
            notesAnn.spanStyles.any {
                notesAnn.text.substring(it.start, it.end) == "important" &&
                    it.item.textDecoration == TextDecoration.Underline
            },
        )

        val procAnn = htmlFragmentToAnnotatedString(parsed.procedure)
        assertTrue(procAnn.text.contains("warning"), procAnn.text)
        assertTrue(procAnn.text.contains("°F"), procAnn.text)
        assertTrue(procAnn.text.contains("First step."), procAnn.text)
        assertFalse(procAnn.text.contains("<strong>"), procAnn.text)
        assertFalse(procAnn.text.contains("&deg;"), procAnn.text)
        assertTrue(
            procAnn.spanStyles.any {
                procAnn.text.substring(it.start, it.end) == "warning" &&
                    it.item.color == Color.Red
            },
            "color span missing: ${procAnn.spanStyles}",
        )
    }
}
