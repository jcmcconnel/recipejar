package recipejar.macro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacroProcessorTest {

    @Test
    fun apply_selection_wrap() {
        val out = MacroProcessor.applyMacro(
            template = "<strong>[SELECTION]</strong>",
            selection = "hello",
        )
        assertEquals("<strong>hello</strong>", out)
    }

    @Test
    fun apply_selection_case_insensitive() {
        val out = MacroProcessor.applyMacro(
            template = """<span style="text-decoration: underline;">[Selection]</span>""",
            selection = "x",
        )
        assertEquals("""<span style="text-decoration: underline;">x</span>""", out)
    }

    @Test
    fun apply_input_and_selection() {
        val out = MacroProcessor.applyMacro(
            template = """<a href="[INPUT:Address]">[SELECTION]</a>""",
            selection = "click",
            inputProvider = { prompt ->
                assertEquals("Address", prompt)
                "https://example.com"
            },
        )
        assertEquals("""<a href="https://example.com">click</a>""", out)
    }

    @Test
    fun apply_color() {
        val out = MacroProcessor.applyMacro(
            template = """<span style="color: [COLOR:Select Text Color];">[SELECTION]</span>""",
            selection = "red",
            colorProvider = { prompt ->
                assertEquals("Select Text Color", prompt)
                "#FF0000"
            },
        )
        assertEquals("""<span style="color: #FF0000;">red</span>""", out)
    }

    @Test
    fun apply_literal_only() {
        val out = MacroProcessor.applyMacro(
            template = "&deg;F",
            selection = "ignored",
        )
        assertEquals("&deg;F", out)
    }

    @Test
    fun apply_unknown_brackets_as_text() {
        val out = MacroProcessor.applyMacro(
            template = "[NOT_A_COMMAND][SELECTION]",
            selection = "y",
        )
        assertEquals("[NOT_A_COMMAND]y", out)
    }
}

class MacroIoTest {

    @Test
    fun parse_macros_txt_defaults() {
        val sample = """
            ; comment
            Bold, B, B,DEFAULT,<strong>[SELECTION]</strong>
            Link,K,K,DEFAULT,<a href="[INPUT:Address]">[SELECTION]</a>
        """.trimIndent()
        val macros = MacroIo.parseMacrosTxt(sample)
        assertEquals(2, macros.size)
        assertEquals("Bold", macros[0].name)
        assertEquals("<strong>[SELECTION]</strong>", macros[0].text)
        assertEquals("B", macros[0].mnemonic)
        assertEquals("Link", macros[1].name)
        assertTrue(macros[1].text.contains("[INPUT:Address]"))
    }

    @Test
    fun parse_macros_txt_commas_in_text() {
        val sample = "X, X, X,DEFAULT,a,b,c"
        val macros = MacroIo.parseMacrosTxt(sample)
        assertEquals(1, macros.size)
        assertEquals("a,b,c", macros[0].text)
    }

    @Test
    fun json_roundtrip() {
        val original = MacroIo.DEFAULT_MACROS
        val encoded = MacroIo.toJson(original)
        val decoded = MacroIo.fromJson(encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun json_bare_array() {
        val encoded = """[{"name":"Bold","text":"<b>[SELECTION]</b>"}]"""
        val decoded = MacroIo.fromJson(encoded)
        assertEquals(1, decoded.size)
        assertEquals("Bold", decoded[0].name)
    }
}
